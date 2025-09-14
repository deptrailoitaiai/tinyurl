package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPageResponse<T> {
    private List<T> content; // Danh sách các items
    private Long nextCursor; // Cursor cho page tiếp theo (null nếu là page cuối)
    private boolean hasNext; // Có page tiếp theo hay không
    private int size; // Số lượng items trong page hiện tại
    
    public static <T> CursorPageResponse<T> of(List<T> content, Long nextCursor, boolean hasNext) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(content.size())
                .build();
    }
    
    public static <T> CursorPageResponse<T> empty() {
        return CursorPageResponse.<T>builder()
                .content(List.of())
                .nextCursor(null)
                .hasNext(false)
                .size(0)
                .build();
    }
}