package me.minidigger.voxelgameslib.api.module;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import me.minidigger.voxelgameslib.api.handler.Handler;

import lombok.extern.java.Log;

/**
 * The module handler takes care of all modules.
 */
@Log
@Singleton
public class ModuleHandler implements Handler {

    @Nonnull
    private List<Module> modules = new ArrayList<>();

    @Inject
    private Injector injector;

    @Override
    public void start() {
        log.info("Loading modules");
        findModules();

        modules.forEach(Module::enable);
    }

    @Override
    public void stop() {
        modules.forEach(Module::disable);
        modules.clear();
    }

    private void findModules() {
        Set<Class<?>> modules = new Reflections().getTypesAnnotatedWith(ModuleInfo.class);

        for (Class<?> clazz : modules) {
            ModuleInfo info = clazz.getAnnotation(ModuleInfo.class);
            if (info == null) continue; // should not occur
            log.info("Loading module " + info.name() + " v" + info.version() + " by " + Arrays.toString(info.authors()));
            if (Module.class.isAssignableFrom(clazz)) {
                Module module = (Module) injector.getInstance(clazz);
                this.modules.add(module);
            } else {
                log.warning("Class " + clazz.getSimpleName() + " has the ModuleInfo annotation but does not implement Module!");
            }
        }

        log.info("Loaded " + modules.size() + " modules!");
    }
}
