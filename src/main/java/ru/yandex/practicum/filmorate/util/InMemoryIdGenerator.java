package ru.yandex.practicum.filmorate.util;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class InMemoryIdGenerator implements IdGenerator {

    private long currentId = 0L;

    @Override
    public Long nextId() {
        return ++currentId;
    }
}
