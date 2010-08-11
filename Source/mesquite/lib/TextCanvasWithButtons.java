package mesquite.lib;

import java.awt.Button;
import mesquite.lib.*;

public class TextCanvasWithButtons {
	Button button = null;
	Button button2 = null;
	MesquiteTextCanvas textCanvas = null;

	public  TextCanvasWithButtons() {
	}

	public Button getButton() {
		return button;
	}

	public void setButton(Button button) {
		this.button = button;
	}

	public Button getButton2() {
		return button2;
	}

	public void setButton2(Button button2) {
		this.button2 = button2;
	}

	public MesquiteTextCanvas getTextCanvas() {
		return textCanvas;
	}

	public void setTextCanvas(MesquiteTextCanvas textCanvas) {
		this.textCanvas = textCanvas;
	}
	

}
