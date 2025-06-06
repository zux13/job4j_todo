package ru.job4j.todo.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.todo.dto.TaskDto;
import ru.job4j.todo.dto.TaskListDto;
import ru.job4j.todo.model.Category;
import ru.job4j.todo.model.Task;
import ru.job4j.todo.model.User;
import ru.job4j.todo.store.CategoryStore;
import ru.job4j.todo.store.PriorityStore;
import ru.job4j.todo.store.TaskStore;
import ru.job4j.todo.util.TimeZoneUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SimpleTaskService implements TaskService {

    private final TaskStore simpleTaskStore;
    private final PriorityStore simplePriorityStore;
    private final CategoryStore simpleCategoryStore;

    @Override
    public Task save(TaskDto taskDto) {
        return simpleTaskStore.save(toEntity(taskDto));
    }

    @Override
    public void update(TaskDto taskDto) {
        simpleTaskStore.update(toEntity(taskDto));
    }

    @Override
    public void delete(int id) {
        simpleTaskStore.delete(id);
    }

    @Override
    public List<TaskListDto> getAllForUserTimezone(User user) {
        return simpleTaskStore.findAll()
                .stream()
                .map(task -> toListDto(task, user))
                .toList();
    }

    @Override
    public List<TaskListDto> findByStatusForUserTimezone(boolean done, User user) {
        return simpleTaskStore.findByStatus(done)
                .stream()
                .map(task -> toListDto(task, user))
                .toList();
    }

    @Override
    public TaskDto findById(int id, boolean editing) {
        Task task = simpleTaskStore.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Задача с id " + id + " не найдена"));
        return toDto(task, editing);
    }

    @Override
    public void markDone(int id) {
        simpleTaskStore.markDone(id);
    }

    private Task toEntity(TaskDto dto) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setCreated(dto.getCreated());
        task.setDone(dto.isDone());
        task.setUser(dto.getUser());
        task.setPriority(
                simplePriorityStore.findById(dto.getPriorityId())
                        .orElseThrow(
                                () -> new NoSuchElementException("Приоритет с id " + dto.getPriorityId() + "не найден")
                        )
        );
        task.setCategories(
                new HashSet<>(
                        simpleCategoryStore.findByIds(dto.getCategoryIds())
                )
        );
        return task;
    }

    private TaskDto toDto(Task task, boolean editing) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setCreated(task.getCreated());
        dto.setDone(task.isDone());
        dto.setEditing(editing);
        dto.setUser(task.getUser());
        dto.setPriorityId(task.getPriority().getId());
        dto.setCategoryIds(
                task.getCategories()
                        .stream()
                        .map(Category::getId)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    private TaskListDto toListDto(Task task, User user) {
        TaskListDto dto = new TaskListDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDone(task.isDone());
        dto.setPriorityId(task.getPriority().getId());
        dto.setAuthorName(
                task.getUser() == null
                        ? "Без автора"
                        : task.getUser().getName()
        );
        dto.setCreatedFormatted(
                TimeZoneUtils.formatZonedDateTimeToUserZone(task.getCreated(), user.getTimezone())
        );
        return dto;
    }

}
