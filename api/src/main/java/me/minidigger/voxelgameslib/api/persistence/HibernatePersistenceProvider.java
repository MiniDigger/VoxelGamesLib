package me.minidigger.voxelgameslib.api.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.reflections.Reflections;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.inject.Inject;
import javax.persistence.Entity;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

import me.minidigger.voxelgameslib.api.config.GlobalConfig;
import me.minidigger.voxelgameslib.api.lang.Locale;
import me.minidigger.voxelgameslib.api.user.User;
import me.minidigger.voxelgameslib.api.user.UserData;

import lombok.extern.java.Log;

/**
 * A implementation of the persistence provider based on hibernate
 */
@Log
public class HibernatePersistenceProvider implements PersistenceProvider {

    @Inject
    private GlobalConfig config;

    private SessionFactory sessionFactory;

    @Override
    public void start() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.username", config.persistence.user)
                .applySetting("hibernate.connection.password", config.persistence.pass)
                .applySetting("hibernate.connection.driver_class", config.persistence.driver)
                .applySetting("hibernate.connection.url", config.persistence.url)
                .applySetting("hibernate.dialect", config.persistence.dialect)
                .applySetting("hibernate.connection.pool_size", config.persistence.pool_size + "")
                .applySetting("hibernate.hbm2ddl.auto", "update")
                .applySetting("hibernate.show_sql", config.persistence.showSQL + "")
                .build();

        MetadataSources sources = new MetadataSources(registry);

        new Reflections().getTypesAnnotatedWith(Entity.class).forEach(sources::addAnnotatedClass);

        try {
            Metadata metadata = sources.buildMetadata();
            sessionFactory = metadata.buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            e.printStackTrace();
        }
    }

    @Override
    public void saveUserData(User user) {
        session((SessionExecutor<Void>) session -> {
            session.saveOrUpdate(user.getData());
            return null;
        });
    }

    @Override
    public Optional<UserData> loadUserData(UUID id) {
        return session(session -> Optional.ofNullable(session.get(UserData.class, id.toString())));
    }

    @Override
    public void saveLocale(Locale locale) {
        session((SessionExecutor<Void>) session -> {
            session.saveOrUpdate(locale);
            return null;
        });
    }

    @Override
    public List<Locale> loadLocales() {
        return session(session -> {
            CriteriaQuery<Locale> criteriaQuery = session.getCriteriaBuilder().createQuery(Locale.class);
            CriteriaQuery<Locale> select = criteriaQuery.select(criteriaQuery.from(Locale.class));
            TypedQuery<Locale> typedQuery = session.createQuery(select);
            return typedQuery.getResultList();
        });
    }

    private <T> T session(SessionExecutor<T> executor) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        T t = executor.execute(session);

        session.getTransaction().commit();
        session.close();

        return t;
    }

    interface SessionExecutor<T> {
        T execute(Session session);
    }

    @Override
    public void stop() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
