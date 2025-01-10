package mesquite.lib.ui;

import java.text.DecimalFormatSymbols;

public class EnglishDecimalFormatSymbols extends DecimalFormatSymbols {
	
	public EnglishDecimalFormatSymbols() {
		setDecimalSeparator('.');
		setExponentSeparator("E");
		setMinusSign('-');
		setGroupingSeparator(',');
		setZeroDigit('0');
	}

}
