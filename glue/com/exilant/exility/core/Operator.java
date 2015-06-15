/* *******************************************************************************************************
Copyright (c) 2015 EXILANT Technologies Private Limited

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ******************************************************************************************************** */
package com.exilant.exility.core;

import java.util.HashMap;
import java.util.Map;

/***
 * represents operators that are used in Expression. Went through several rounds
 * of re-factoring, starting with simple enums
 * 
 */
public abstract class Operator {
	static final Operator ADD = new AddOperator();
	static final Operator SUBTRACT = new SubtractOperator();
	static final Operator MULTIPLY = new MultiplyOperator();
	static final Operator DIVIDE = new DivideOperator();
	static final Operator REMAINDER = new RemainderOperator();
	static final Operator POWER = new PowerOperator();
	static final Operator EQUAL = new EqualOperator();
	static final Operator NOT_EQUAL = new NotEqualOperator();
	static final Operator LESS_THAN = new LessThanOperator();
	static final Operator LESS_THAN_OR_EQUAL = new LessThanOrEqualOperator();
	static final Operator GREATER_THAN = new GreaterThanOperator();
	static final Operator GREATER_THAN_OR_EQUAL = new GreaterThanOrEqualOperator();
	static final Operator NOT = new NotOperator();
	static final Operator AND = new AndOperator();
	static final Operator OR = new OrOperator();
	static final Operator EXISTS = new ExistsOperator();
	static final Operator NOT_EXISTS = new NotExistsOperator();

	static final int LAST_PRECEDENCE_LEVEL = 5;
	private static Map<String, Operator> operatorTexts = new HashMap<String, Operator>();
	private static Operator[] VALUES = { Operator.ADD, Operator.SUBTRACT,
			Operator.MULTIPLY, Operator.DIVIDE, Operator.REMAINDER,
			Operator.POWER, Operator.EQUAL, Operator.NOT_EQUAL,
			Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUAL,
			Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUAL,
			Operator.NOT, Operator.AND, Operator.OR, Operator.EXISTS,
			Operator.NOT_EXISTS };

	static {
		for (Operator oper : Operator.VALUES) {
			Operator.operatorTexts.put(oper.toString(), oper);
		}
	}

	/**
	 * an array of all operators we have defined
	 * 
	 * @return an array of all operators we have defined
	 */
	public static Operator[] values() {
		return Operator.VALUES;
	}

	/**
	 * parse into an operator
	 * 
	 * @param value
	 * @param startAt
	 * @return parsed operator or null in case of error
	 */
	public static Operator parse(String value, int startAt) {
		// first try the ones with two chars
		int max = startAt + 2;
		Operator oper = null;
		String opText = null;
		if (value.length() >= max) {
			opText = value.substring(startAt, startAt + 2);
			if (Operator.operatorTexts.containsKey(opText)) {
				oper = Operator.operatorTexts.get(opText);
			}
		}
		if (oper == null) {
			opText = value.substring(startAt, startAt + 1);
			if (Operator.operatorTexts.containsKey(opText)) {
				oper = Operator.operatorTexts.get(opText);
			}
		}
		return oper;
	}

	abstract int getPrecedence();

	/**
	 * return value type if this operator is found with the supplied left and
	 * right operands
	 * 
	 * @param leftType
	 *            type of left operand
	 * @param rightType
	 *            type of right operand
	 * @return type of returned value
	 */
	abstract public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType);

	/**
	 * 
	 * @param operand1
	 * @return
	 * @throws ExilityException
	 */
	Value operate(Value operand1) throws ExilityException {
		throw new ExilityException("Operator " + this.toString()
				+ " is not a unary operator");
	}

	/**
	 * 
	 * @param operand1
	 * @param operand2
	 * @return
	 * @throws ExilityException
	 */
	Value operate(Value operand1, Value operand2) throws ExilityException {
		throw new ExilityException("Operator " + this.toString()
				+ " is a unary operator");
	}

	void raiseException(Value val1, Value val2) throws ExilityException {
		throw new ExilityException("Operator " + this.toString()
				+ " is not applicable between type " + val1.getValueType()
				+ " and " + val2.getValueType());
	}

	void raiseException(Value val1) throws ExilityException {
		throw new ExilityException("Unary operator " + this.toString()
				+ " is not applicable for value of type " + val1.getValueType());
	}
}

class AddOperator extends Operator {
	@Override
	int getPrecedence() {
		return 3;
	}

	@Override
	public String toString() {
		return "+";
	}

	@Override
	Value operate(Value val1) throws ExilityException {
		DataValueType type = val1.getValueType();
		if (type == DataValueType.INTEGRAL || type == DataValueType.DECIMAL) {
			return val1;
		}
		this.raiseException(val1);
		return null;
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.add(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		if (leftType == DataValueType.TEXT || rightType == DataValueType.TEXT) {
			return DataValueType.TEXT;
		}
		if (leftType == DataValueType.DATE) {
			return DataValueType.DATE;
		}
		if (leftType == DataValueType.DECIMAL
				|| rightType == DataValueType.DECIMAL) {
			return DataValueType.DECIMAL;
		}
		return DataValueType.INTEGRAL;
	}
}

class SubtractOperator extends Operator {
	@Override
	int getPrecedence() {
		return 3;
	}

	@Override
	public String toString() {
		return "-";
	}

	@Override
	Value operate(Value val1) throws ExilityException {
		DataValueType type = val1.getValueType();
		if (type == DataValueType.INTEGRAL) {
			return Value.newValue(-val1.getIntegralValue());
		} else if (type == DataValueType.DECIMAL) {
			return Value.newValue(-val1.getDecimalValue());
		}

		this.raiseException(val1);
		return null;
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.subtract(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		if (leftType == DataValueType.DATE) {
			if (rightType == DataValueType.DATE) {
				return DataValueType.INTEGRAL;
			}
			return DataValueType.DATE;
		}
		if (leftType == DataValueType.DECIMAL
				|| rightType == DataValueType.DECIMAL) {
			return DataValueType.DECIMAL;
		}
		return DataValueType.INTEGRAL;
	}
}

class MultiplyOperator extends Operator {
	@Override
	int getPrecedence() {
		return 2;
	}

	@Override
	public String toString() {
		return "*";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.multiply(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		if (leftType == DataValueType.DECIMAL
				|| rightType == DataValueType.DECIMAL) {
			return DataValueType.DECIMAL;
		}
		return DataValueType.INTEGRAL;
	}
}

class DivideOperator extends Operator {
	@Override
	int getPrecedence() {
		return 2;
	}

	@Override
	public String toString() {
		return "/";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.divide(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		if (leftType == DataValueType.DECIMAL
				|| rightType == DataValueType.DECIMAL) {
			return DataValueType.DECIMAL;
		}
		return DataValueType.INTEGRAL;
	}
}

class RemainderOperator extends Operator {
	@Override
	int getPrecedence() {
		return 2;
	}

	@Override
	public String toString() {
		return "%";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.remainder(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.INTEGRAL;
	}
}

class PowerOperator extends Operator {
	@Override
	int getPrecedence() {
		return 1;
	}

	@Override
	public String toString() {
		return "^";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.power(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		if (leftType == DataValueType.DECIMAL
				|| rightType == DataValueType.DECIMAL) {
			return DataValueType.DECIMAL;
		}
		return DataValueType.INTEGRAL;
	}
}

class OrOperator extends Operator {
	@Override
	int getPrecedence() {
		return 5;
	}

	@Override
	public String toString() {
		return "|";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.or(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class AndOperator extends Operator {
	@Override
	int getPrecedence() {
		return 5;
	}

	@Override
	public String toString() {
		return "&";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.and(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class EqualOperator extends Operator {
	@Override
	int getPrecedence() {
		return 4;
	}

	@Override
	public String toString() {
		return "=";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.equal(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class NotEqualOperator extends Operator {
	@Override
	int getPrecedence() {
		return 4;
	}

	@Override
	public String toString() {
		return "!=";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.notEqual(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class GreaterThanOperator extends Operator {
	@Override
	int getPrecedence() {
		return 4;
	}

	@Override
	public String toString() {
		return ">";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.greaterThan(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class GreaterThanOrEqualOperator extends Operator {
	@Override
	int getPrecedence() {
		return 4;
	}

	@Override
	public String toString() {
		return ">=";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.greaterThanOrEqual(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class LessThanOperator extends Operator {
	@Override
	int getPrecedence() {
		return 4;
	}

	@Override
	public String toString() {
		return "<";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.lessThan(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class LessThanOrEqualOperator extends Operator {
	@Override
	int getPrecedence() {
		return 4;
	}

	@Override
	public String toString() {
		return "<=";
	}

	@Override
	Value operate(Value val1, Value val2) throws ExilityException {
		return val1.lessThanOrEqual(val2);
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class NotOperator extends Operator {
	@Override
	int getPrecedence() {
		return 0;
	}

	@Override
	public String toString() {
		return "!";
	}

	@Override
	Value operate(Value val1) throws ExilityException {
		if (val1.getValueType() != DataValueType.BOOLEAN) {
			this.raiseException(val1);
			return null;
		}
		if (val1.getBooleanValue()) {
			return BooleanValue.FALSE_VALUE;
		}
		return BooleanValue.TRUE_VALUE;

	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class ExistsOperator extends Operator {
	@Override
	int getPrecedence() {
		return 0;
	}

	@Override
	public String toString() {
		return "?";
	}

	@Override
	Value operate(Value val1) {
		if (val1 == null || val1.isNull()) {
			return BooleanValue.FALSE_VALUE;
		}

		return BooleanValue.TRUE_VALUE;
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}

class NotExistsOperator extends Operator {
	@Override
	int getPrecedence() {
		return 0;
	}

	@Override
	public String toString() {
		return "!?";
	}

	@Override
	Value operate(Value val1) {
		if (val1 == null || val1.isNull()) {
			return BooleanValue.TRUE_VALUE;
		}

		return BooleanValue.FALSE_VALUE;
	}

	@Override
	public DataValueType getValueType(DataValueType leftType,
			DataValueType rightType) {
		return DataValueType.BOOLEAN;
	}
}
