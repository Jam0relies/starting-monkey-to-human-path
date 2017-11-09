package po53.kuznetsov.wdad.data.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class PreferencesManagerTest {

    @Test
    void registry() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();

        Registry newRegistry = new Registry(true, "someAddres.com", 80);

        preferencesManager.addRegistry(newRegistry);
        List<Registry> registries = preferencesManager.getRegistries();
        assertTrue(registries.contains(newRegistry));

        BindedObject bindedObject = new BindedObject("someClass", "someName");
        preferencesManager.addBindedObject(newRegistry,bindedObject);
        List<BindedObject> bindedObjects = preferencesManager.getBindedObjects();
        assertTrue(bindedObjects.contains(bindedObject));
        bindedObjects = preferencesManager.getBindedObjects(newRegistry);
        assertTrue(bindedObjects.contains(bindedObject));

        assertTrue(preferencesManager.removeBindedObject(bindedObject));

        bindedObjects = preferencesManager.getBindedObjects();
        assertFalse(bindedObjects.contains(bindedObject));
        bindedObjects = preferencesManager.getBindedObjects(newRegistry);
        assertFalse(bindedObjects.contains(bindedObject));


        assertTrue(preferencesManager.removeRegistry(newRegistry));
        registries = preferencesManager.getRegistries();
        assertFalse(registries.contains(newRegistry));

    }

    @Test
    void classProvider() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();

        String oldValue = preferencesManager.getClassProvider();

        String newValue = "newValue";
        preferencesManager.setClassProvider(newValue);
        assertEquals(newValue, preferencesManager.getClassProvider());

        preferencesManager.setClassProvider(oldValue);
        assertEquals(oldValue, preferencesManager.getClassProvider());
    }


    @Test
    void hasClassProvider() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();
        assertTrue(preferencesManager.hasClassProvider());
    }

    @Test
    void getInstance() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();
        assertNotNull(preferencesManager);
    }

    @Test
    void policyPath() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();

        String oldValue = preferencesManager.getPolicyPath();

        String newValue = ".somePath";
        preferencesManager.setPolicyPath(newValue);
        assertEquals(newValue, preferencesManager.getPolicyPath());

        preferencesManager.setPolicyPath(oldValue);
        assertEquals(oldValue, preferencesManager.getPolicyPath());
    }

    @Test
    void isUseCodebaseOnly() {
        PreferencesManager preferencesManager = PreferencesManager.getInstance();

        testBooleanProperty(preferencesManager::isUseCodebaseOnly,
                preferencesManager::setUseCodebaseOnly);
    }


    void testBooleanProperty(Supplier<Boolean> getMethod, Consumer<Boolean> setMethod) {

        boolean oldValue = getMethod.get();

        setMethod.accept(true);
        assertTrue(getMethod.get());

        setMethod.accept(false);
        assertFalse(getMethod.get());

        setMethod.accept(oldValue);
        assertEquals(oldValue, getMethod.get());

    }


}