package com.epam.lstrsum.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class QueryErrorDefinitionDto {
    Integer indexFrom;
    Integer indexTo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryErrorDefinitionDto that = (QueryErrorDefinitionDto) o;

        if (!indexFrom.equals(that.indexFrom)) return false;
        return indexTo.equals(that.indexTo);
    }

    @Override
    public int hashCode() {
        int result = indexFrom.hashCode();
        result = 31 * result + indexTo.hashCode();
        return result;
    }


}
