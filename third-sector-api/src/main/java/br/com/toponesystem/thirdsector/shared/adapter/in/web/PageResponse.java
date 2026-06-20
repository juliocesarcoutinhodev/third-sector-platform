package br.com.toponesystem.thirdsector.shared.adapter.in.web;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {

    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> springPage) {
        return new PageResponse<>(
                springPage.getContent(),
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                springPage.getNumber(),
                springPage.getSize()
        );
    }
}
