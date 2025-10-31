package ru.homecrew.config.quarts;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;
import org.quartz.spi.TriggerFiredBundle;
import org.quartz.utils.DBConnectionManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Конфигурация планировщика Quartz для проекта HomeCrew.
 * Настраивает подключение к БД, интеграцию со Spring и фабрику Job-ов.
 */
@Configuration
public class QuartzConfig {

    /**
     * Основной бин фабрики Quartz Scheduler.
     * Регистрирует DataSource, JobFactory и базовые Quartz-свойства.
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource, ApplicationContext applicationContext) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory(applicationContext));
        factory.setOverwriteExistingJobs(true);
        factory.setStartupDelay(3);
        factory.setQuartzProperties(quartzProperties());

        registerDataSource(dataSource);

        return factory;
    }

    /**
     * Настройки Quartz (JobStore, кластеризация, имя инстанса и т.д.).
     */
    private Properties quartzProperties() {
        Properties props = new Properties();
        props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        props.setProperty("org.quartz.jobStore.isClustered", "false");
        props.setProperty("org.quartz.scheduler.instanceName", "HomeCrewScheduler");
        props.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        props.setProperty("org.quartz.jobStore.dataSource", "dataSource");
        return props;
    }

    /**
     * Явная регистрация DataSource в менеджере подключений Quartz.
     * Без этого Quartz не сможет использовать Spring DataSource напрямую.
     */
    private void registerDataSource(DataSource dataSource) {
        DBConnectionManager.getInstance()
                .addConnectionProvider("dataSource", new org.quartz.utils.ConnectionProvider() {
                    @Override
                    public Connection getConnection() throws SQLException {
                        return dataSource.getConnection();
                    }

                    @Override
                    public void shutdown() {
                        // Spring сам управляет DataSource
                    }

                    @Override
                    public void initialize() {
                        // Spring сам инициализирует DataSource
                    }
                });
    }

    /**
     * Фабрика для создания Quartz Job-ов с автосвязыванием Spring-зависимостей.
     */
    private SpringBeanJobFactory jobFactory(ApplicationContext context) {
        return new AutowiringSpringBeanJobFactory(context);
    }

    /**
     * Кастомная JobFactory, которая внедряет зависимости из Spring-контекста
     * в создаваемые Quartz Job-объекты.
     */
    static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {
        private final ApplicationContext context;

        AutowiringSpringBeanJobFactory(ApplicationContext context) {
            this.context = context;
        }

        @Override
        @NonNull
        protected Object createJobInstance(@NonNull TriggerFiredBundle bundle) throws Exception {
            Object job = super.createJobInstance(bundle);
            context.getAutowireCapableBeanFactory().autowireBean(job);
            return job;
        }
    }
}
