package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageRequest {
    private Long cursor; // ID của record cuối cùng từ page trước (null cho page đầu tiên)
    private int limit; // Số lượng records muốn lấy
    
    @Builder.Default
    private SortDirection direction = SortDirection.ASC; // Hướng sắp xếp
    
    public enum SortDirection {
        ASC, DESC
    }
    
    public static CursorPageRequest of(Long cursor, int limit) {
        return CursorPageRequest.builder()
                .cursor(cursor)
                .limit(limit)
                .build();
    }
    
    public static CursorPageRequest of(Long cursor, int limit, SortDirection direction) {
        return CursorPageRequest.builder()
                .cursor(cursor)
                .limit(limit)
                .direction(direction)
                .build();
    }
}