package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import src.GameManager;

public class TestGameManager {
    @Test
    public void testWaitForChooserChangeReturnsUpdatedChooser() {
        GameManager manager = new GameManager(true);
        int lastIndex = manager.getNextToChoose();

        manager.incrementChooser();

        int updatedIndex = manager.waitForChooserChange(lastIndex, 1000);
        assertTrue(updatedIndex != lastIndex);
        assertEquals(1, updatedIndex);
    }

    @Test
    public void testWaitForChooserChangeTimesOutWithoutUpdate() {
        GameManager manager = new GameManager(true);
        int currentIndex = manager.getNextToChoose();

        int updatedIndex = manager.waitForChooserChange(currentIndex, 50);
        assertEquals(currentIndex, updatedIndex);
    }
}
