package truelecter.iig.screen.visual;

import truelecter.iig.util.Function;

import com.badlogic.gdx.graphics.Texture;

public class Checkbox extends Button {

    private Texture checked;
    private Texture unchecked;
    private boolean isChecked;
    private Function onCheck;
    private Function onUncheck;

    public Checkbox(float x, float y, float width, float height, Texture checked, Texture unchecked, boolean isChecked,
            Function onClick, Function onCheck, Function onUncheck) {
        super(isChecked ? checked : unchecked, x, y, width, height, onClick);
        this.checked = checked;
        this.unchecked = unchecked;
        this.onCheck = onCheck;
        this.onUncheck = onUncheck;
    }

    @Override
    protected boolean checkIfClicked(float ix, float iy) {
        if (ix > x - origWidth * scale / 2 && ix < x + origWidth * scale / 2) {
            if (iy > y - scale * origHeight / 2 && iy < y + scale * origHeight / 2) {
                click();
                return true;
            }
        }
        return false;
    }

    @Override
    public void click() {
        isChecked = !isChecked;
        if (isChecked) {
            super.changeSkin(checked, origWidth, origHeight);
            if (onCheck != null) {
                onCheck.toRun();
            }
        } else {
            super.changeSkin(unchecked, origWidth, origHeight);
            if (onUncheck != null) {
                onUncheck.toRun();
            }
        }
        super.click();
    }

    public boolean isChecked() {
        return isChecked;
    }

    public int getPiority() {
        return 99;
    }
}
