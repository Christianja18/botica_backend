package com.botica.botica.service.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class OrderedPageMapper {

    private OrderedPageMapper() {
    }

    public static <ID, T> Page<T> map(Page<ID> idPage,
                                      Pageable pageable,
                                      Collection<T> entities,
                                      Function<T, ID> idExtractor) {
        if (idPage.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, idPage.getTotalElements());
        }

        Map<ID, T> byId = entities.stream()
                .collect(Collectors.toMap(idExtractor, Function.identity()));

        return new PageImpl<>(
                idPage.getContent().stream()
                        .map(byId::get)
                        .filter(Objects::nonNull)
                        .toList(),
                pageable,
                idPage.getTotalElements()
        );
    }
}
