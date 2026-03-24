package com.smartexpense.model.base;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract base entity for all domain objects.
 * Demonstrates OOP: Abstraction — common fields shared by all entities.
 */
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Long id;
    protected LocalDateTime createdAt;

    public BaseEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public BaseEntity(Long id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
    }

    // Abstract method — subclasses must provide entity type name
    public abstract String getEntityType();

    @Override
    public String toString() {
        return getEntityType() + "{id=" + id + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
