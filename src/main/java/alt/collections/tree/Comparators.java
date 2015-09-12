package alt.collections.tree;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

/**
 * Simple comparators for primitive types
 * 
 * @author Albert Shift
 *
 */

public interface Comparators {

	Comparator<Boolean> BOOLEAN = new Comparator<Boolean>() {

		@Override
		public int compare(Boolean o1, Boolean o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<Byte> BYTE = new Comparator<Byte>() {

		@Override
		public int compare(Byte o1, Byte o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<Character> CHAR = new Comparator<Character>() {

		@Override
		public int compare(Character o1, Character o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<Short> SHORT = new Comparator<Short>() {

		@Override
		public int compare(Short o1, Short o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<Integer> INTEGER = new Comparator<Integer>() {

		@Override
		public int compare(Integer o1, Integer o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<Long> LONG = new Comparator<Long>() {

		@Override
		public int compare(Long o1, Long o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<Float> FLOAT = new Comparator<Float>() {

		@Override
		public int compare(Float o1, Float o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<Double> DOUBLE = new Comparator<Double>() {

		@Override
		public int compare(Double o1, Double o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<BigInteger> BIG_INTEGER = new Comparator<BigInteger>() {

		@Override
		public int compare(BigInteger o1, BigInteger o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<BigDecimal> BIG_DECIMAL = new Comparator<BigDecimal>() {

		@Override
		public int compare(BigDecimal o1, BigDecimal o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<Date> DATE = new Comparator<Date>() {

		@Override
		public int compare(Date o1, Date o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
	Comparator<String> STRING = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			}
			return o1.compareTo(o2);
		}
		
	};
	
}
