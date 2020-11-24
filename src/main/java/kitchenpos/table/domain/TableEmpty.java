package kitchenpos.table.domain;

import javax.persistence.Embeddable;

import kitchenpos.table.dto.InvalidTableEmptyException;

@Embeddable
public class TableEmpty {

    private Boolean empty;

    public TableEmpty() {
    }

    public TableEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void changeEmpty(boolean empty, boolean hasGroup) {
        if (hasGroup) {
            throw new InvalidTableEmptyException();
        }
        this.empty = empty;
    }

    public void fill() {
        this.empty = false;
    }

    public boolean existCustomer() {
        return !empty;
    }
}
