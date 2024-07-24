package neurord.model;

import java.util.Map;
import java.util.Stack;

/**
 * This class represents methods that resolve the parameter values taken from
 * the input file. The values can be a combination of numerical values, other variables,
 * as well as the basic math operators and chars: ^()*+/-
 * 
 * @version 1.1
 */

public class ParameterResolver {

    public Double constructPValue(String pValue, Map<String, Double> parameters) throws Exception {
        // Split the pValue into elements
        String[] parts = pValue.split("(?=[-+*/^()])|(?<=[^-+*/^][-+*/^])|(?<=[()])");
        
        return evaluateExpression(parts, parameters);
    }

    private Double evaluateExpression(String[] parts, Map<String, Double> parameters) throws Exception {
        // If there is only one element
        if (parts.length == 1) {
            String part = parts[0];
            if (parameters.containsKey(part)) {
                return parameters.get(part);
            } else {
                try {
                    return Double.parseDouble(part);
                } catch (NumberFormatException e) {
                    throw new Exception("Invalid number format: " + part);
                }
            }
        }

        // Handle parentheses if existed
        Stack<String> stack = new Stack<>();
        int left = -1, right = -1;
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("(")) {
                left = i;
            } else if (parts[i].equals(")")) {
                right = i;
                if (left != -1 && right != -1) {
                    for (int j = left + 1; j < right; j++) {
                        stack.push(parts[j]);
                    }
                    double value = evaluateStack(stack, parameters);
                    parts[left] = String.valueOf(value);

                    // Shift elements to the left to remove the evaluated expression
                    for (int j = right + 1, k = left + 1; j < parts.length; j++, k++) {
                    	parts[k] = parts[j];
                    }
                    String[] newParts = new String[parts.length - (right - left)];
                    System.arraycopy(parts, 0, newParts, 0, newParts.length);
                    return evaluateExpression(newParts, parameters);
                }
            }
        }

        // If no parentheses, evaluate directly
        for (String part : parts) {
            stack.push(part);
        }
        return evaluateStack(stack, parameters);
    }

    private double evaluateStack(Stack<String> stack, Map<String, Double> parameters) throws Exception {
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        while (!stack.isEmpty()) {
            String part = stack.remove(0);
            if (isNumber(part)) {
                values.push(Double.parseDouble(part));
            } else if (parameters.containsKey(part)) {
                values.push(parameters.get(part));
            } else if (isOperator(part.charAt(0))) {
            	while (!ops.isEmpty() && hasPriority(ops.peek(), part.charAt(0))) {
                    values.push(operation(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(part.charAt(0));
            }
        }

        while (!ops.isEmpty()) {
            values.push(operation(ops.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private boolean isNumber(String part) {
        try {
        	Double.parseDouble(part);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '^';
    }

    // Returns true if op1 has priority over over op2
    private boolean hasPriority(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        if ((op2 == '*' || op2 == '/') && (op1 == '+' || op1 == '-')) {
            return false;
        }
        if (op2 == '^' && op1 != '^') {
            return false;
        }
        return true;
    }

    private double operation(char op, double b, double a) throws Exception {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/':
                if (b == 0) throw new ArithmeticException("Cannot divide by zero");
                return a / b;
            case '^': return (int) Math.pow(a, b);
        }
        return 0;
    }
}
