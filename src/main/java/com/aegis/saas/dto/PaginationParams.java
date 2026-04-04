package com.aegis.saas.dto;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Request parameters documentation for pagination
 * 
 * Usage in requests:
 * - page (0-indexed): ?page=0 (default)
 * - size: ?size=20 (default)
 * - sort: ?sort=createdAt,desc or ?sort=id,asc
 * 
 * Example requests:
 * - GET /api/v1/plans?page=0&size=10&sort=name,asc
 * - GET /api/v1/tenant-admin/user-subscriptions?page=1&size=20&sort=nextBillingDate,asc
 * - GET /api/v1/tenant-admin/sessions?page=0&size=5&sort=issuedAt,desc
 * 
 * Response structure:
 * {
 *   "content": [...],           // List of items
 *   "pageNumber": 0,            // Current page (0-indexed)
 *   "pageSize": 20,             // Items per page
 *   "totalElements": 150,       // Total items in DB
 *   "totalPages": 8,            // Total pages
 *   "hasNext": true,            // Has next page
 *   "hasPrevious": false,       // Has previous page
 *   "isFirst": true,            // Is first page
 *   "isLast": false             // Is last page
 * }
 */
@Data
public class PaginationParams {
    private int page = 0;
    private int size = 20;
    private String sort = "id,desc";

    public Pageable toPageable() {
        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    public static Pageable defaultPageable() {
        return PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));
    }
}
