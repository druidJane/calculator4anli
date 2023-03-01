/**
 * @author zhangzhe
 * @date 2023/3/1
 * 
 */

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Data
@Slf4j
public class Calculator {
    // 前面结果
    private BigDecimal preResult;
    // 当前输入值
    private BigDecimal input;
    // 结果总值列表
    private List<BigDecimal> resultList = Lists.newArrayList();
    // 当前操作符
    private String operator;
    // 结果索引，用于undo，redo，当执行calculate方法，将移除resultList的index比lastResultIndex大的数据
    private int lastResultIndex = -1;
    // 默认精度2位小数
    private static final int SCALE = 2;

    public void setInput(BigDecimal input) {
        // 未计算过,累计总值为第一个输入值
        if(preResult == null){
            preResult = input;
        }else{
            this.input = input;
        }
    }

    /**
     *  计算,相当于计算器的等于按钮
     *  如果执行过undo，redo操作，则移除resultList在lastResultIndex之后的数据
     */
    public void calculate(){
        String start = display();
        preResult = preResult == null ? BigDecimal.ZERO : preResult;
        if(operator == null){
            log.warn("请选择操作!");
        }
        if(input != null){ // 新输入值
            // 累加计算
            BigDecimal ret = doCalculate(preResult, operator, input);
            if(resultList.size()-1 > lastResultIndex){
                //移除resultList在lastResultIndex之后的数据
                resultList = resultList.subList(0, lastResultIndex + 1);
            }
            resultList.add(ret);
            preResult = ret;
            operator = null;
            input = null;
            lastResultIndex++;
        }
        String result = display();
        log.info("{}={}", start, result);
    }

    /**
     * 回撤到上一步
     */
    public void undo(){
        if(resultList.size() == 0){
            log.warn("无操作!");
        }else if(resultList.size() == 1){
            log.info("undo后值:0,"+"undo前值:"+ preResult);
            preResult = BigDecimal.ZERO;
        } else if(lastResultIndex-1 < 0) {
            log.warn("无法再undo!");
            return;
        }else if(lastResultIndex == -1){
            lastResultIndex = resultList.size()-1;
            preResult = resultList.get(lastResultIndex);
        } else{
            lastResultIndex--;
            preResult = resultList.get(lastResultIndex);
        }
        log.info("undo:{}", preResult);
    }

    /**
     *  根据回撤进行重做
     */
    public void redo(){
        try{
            if(lastResultIndex > -1){
                if(lastResultIndex + 1 == resultList.size()){
                    log.warn("无法再redo!");
                    return;
                }
                lastResultIndex++;
                preResult = resultList.get(lastResultIndex);
            }
        }catch (Exception e){
            log.error("redo异常, index:{}", lastResultIndex);
        }
        log.info("redo:{}", preResult);
    }

    /**
     * 进行累计计算
     * @param preTotal 前面已累计值
     * @param curOperator 当前操作
     * @param newNum 新输入值
     * @return 计算结果
     */
    private BigDecimal doCalculate(BigDecimal preTotal, String curOperator, BigDecimal newNum) {
        BigDecimal ret = BigDecimal.ZERO;
        curOperator = curOperator == null ? "+" : curOperator;
        switch (curOperator){
            case "+":
                ret = preTotal.add(newNum);
                break;
            case "-":
                ret = preTotal.subtract(newNum).setScale(SCALE, RoundingMode.HALF_UP);
                break;
            case "*":
                ret = preTotal.multiply(newNum).setScale(SCALE, RoundingMode.HALF_UP);
                break;
            case "/":
                ret = preTotal.divide(newNum, RoundingMode.HALF_UP);
                break;
        }
        return ret;
    }

    /**
     * 显示操作结果
     */
    public String display(){
        StringBuilder sb = new StringBuilder();
        if(preResult != null){
            sb.append(preResult.setScale(SCALE, BigDecimal.ROUND_HALF_DOWN).toString());
        }
        if(operator != null){
            sb.append(operator);
        }
        if(input != null){
            sb.append(input);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        calculator.setInput(new BigDecimal(3));
        calculator.setOperator("+");
        calculator.setInput(new BigDecimal(5));
        calculator.calculate();
        calculator.setOperator("*");
        calculator.setInput(new BigDecimal(2));
        calculator.calculate();
        calculator.undo();

        calculator.setOperator("+");
        calculator.setInput(new BigDecimal(2));
        calculator.calculate();

        calculator.undo();
        calculator.undo();
        calculator.redo();
        calculator.redo();
        calculator.redo();
        calculator.redo();
    }

}