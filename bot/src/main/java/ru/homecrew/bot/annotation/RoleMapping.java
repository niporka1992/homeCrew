package ru.homecrew.bot.annotation;

import java.lang.annotation.*;
import ru.homecrew.enums.Role;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoleMapping {
    Role value();
}
