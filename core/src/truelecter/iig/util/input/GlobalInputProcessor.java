package truelecter.iig.util.input;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;

public class GlobalInputProcessor implements InputProcessor {

    private static ArrayList<SubInputProcessor> processors;
    private static GlobalInputProcessor instance;

    public static GlobalInputProcessor getInstance() {
        if (instance == null) {
            instance = new GlobalInputProcessor();
            Gdx.input.setInputProcessor(instance);
        }
        return instance;
    }

    private static void swap(ArrayList<SubInputProcessor> arr, int i, int j) {
        SubInputProcessor t = arr.get(i);
        arr.set(i, arr.get(j));
        arr.set(j, t);
    }

    private static void sortByPriority() {
        for (int i = getProcessors().size() - 1; i > -1; i--) {
            for (int j = 0; j < i; j++) {
                if (getProcessors().get(j + 1).getPriority() > getProcessors().get(j).getPriority()) {
                    swap(getProcessors(), j + 1, j);
                }
            }
        }
        for (int i = 0; i < getProcessors().size(); i++) {
            System.out.print(getProcessors().get(i).getClass().getSimpleName() + " ");
        }
        System.out.println();
    }

    private static ArrayList<SubInputProcessor> getProcessors() {
        if (processors == null)
            processors = new ArrayList<SubInputProcessor>();
        return processors;
    }

    public static void remove(SubInputProcessor s) {
        getProcessors().remove(s);
        sortByPriority();
    }

    public static void register(SubInputProcessor s) {
        getProcessors().add(s);
        sortByPriority();
    }

    public static void removeAllOfClass(Class<?> clazz) {
        for (SubInputProcessor p : getProcessors()) {
            if (p.getClass().getName().equals(clazz.getName())) {
                remove(p);
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        for (int i = 0; i < processors.size(); i++) {
            SubInputProcessor p = processors.get(i);
            if (p != null && p.keyDown(keycode))
                return true;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        for (int i = 0; i < processors.size(); i++) {
            SubInputProcessor p = processors.get(i);
            if (p != null && p.keyUp(keycode))
                return true;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        for (int i = 0; i < processors.size(); i++) {
            SubInputProcessor p = processors.get(i);
            if (p != null && p.keyTyped(character))
                return true;
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (int i = 0; i < processors.size(); i++) {
            SubInputProcessor p = processors.get(i);
            if (p != null && p.touchDown(screenX, screenY, pointer, button))
                return true;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (int i = 0; i < processors.size(); i++) {
            SubInputProcessor p = processors.get(i);
            if (p != null && p.touchUp(screenX, screenY, pointer, button)) {
                System.out.println("Succeed! " + p.getClass().getName() + " ");
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        for (int i = 0; i < processors.size(); i++) {
            SubInputProcessor p = processors.get(i);
            if (p != null && p.touchDragged(screenX, screenY, pointer))
                return true;
        }
        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        for (int i = 0; i < processors.size(); i++) {
            SubInputProcessor p = processors.get(i);
            if (p != null && p.mouseMoved(screenX, screenY))
                return true;
        }
        return true;
    }

    @Override
    public boolean scrolled(int amount) {
        for (int i = 0; i < processors.size(); i++) {
            SubInputProcessor p = processors.get(i);
            if (p != null && p.scrolled(amount))
                return true;
        }
        return true;
    }
}
