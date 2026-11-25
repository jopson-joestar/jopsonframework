package org.example.jorsy;

import java.util.List;
import java.util.stream.IntStream;

import com.jorsyframework.starter.core.convert.ConvertContext;
import com.jorsyframework.starter.core.convert.ConvertProvider;
import com.jorsyframework.starter.core.enums.ValueEnum;
import com.jorsyframework.starter.core.page.PageQuery;
import com.jorsyframework.starter.core.page.PageResult;
import com.jorsyframework.starter.core.result.R;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
class DocumentEndpoints {
    enum DocumentStatus implements ValueEnum<String> {
        DRAFT("draft"), APPROVED("approved");

        private final String code;
        DocumentStatus(String code) { this.code = code; }
        public String getValue() { return code; }
    }

    enum ReviewLevel implements ValueEnum<Integer> {
        NORMAL(10), URGENT(20);

        private final int code;
        ReviewLevel(int code) { this.code = code; }
        public Integer getValue() { return code; }
    }

    enum Visibility { PUBLIC, PRIVATE }

    record CreateDocumentParam(@NotBlank String number, @NotBlank @Size(max = 80) String title) {}
    record UpdateDocumentParam(@NotBlank @Size(max = 80) String title, @Size(max = 200) String remark) {}
    record DocumentQuery(DocumentStatus status) {}
    record Filters(DocumentStatus status, ReviewLevel level, Visibility visibility) {}
    record Document(int id, DocumentStatus status, String reviewerId) {}
    record DocumentRow(int id, DocumentStatus status, String reviewerName) {}

    private final DocumentRowMapper mapper;
    private final ConvertProvider<String, String> reviewers;

    DocumentEndpoints(DocumentRowMapper mapper, ConvertProvider<String, String> reviewers) {
        this.mapper = mapper;
        this.reviewers = reviewers;
    }

    @PostMapping
    R<CreateDocumentParam> create(@Valid @RequestBody CreateDocumentParam input) {
        return R.success(input);
    }

    @PutMapping("/{id}")
    R<UpdateDocumentParam> update(@PathVariable int id, @Valid @RequestBody UpdateDocumentParam input) {
        return R.success(input);
    }

    @GetMapping("/filters/{level}")
    Filters filters(@PathVariable ReviewLevel level, @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) Visibility visibility) {
        return new Filters(status, level, visibility);
    }

    @PostMapping("/filters")
    Filters filters(@RequestBody Filters filters) {
        return filters;
    }

    @GetMapping
    R<PageResult<DocumentRow>> list(@ModelAttribute DocumentQuery query, @Valid @ModelAttribute PageQuery page) {
        var matching = IntStream.rangeClosed(1, 41)
                .mapToObj(id -> new Document(id, DocumentStatus.APPROVED, switch (id % 3) {
                    case 0 -> "missing";
                    case 1 -> "user-1";
                    default -> null;
                }))
                .filter(row -> query.status() == null || row.status() == query.status())
                .toList();
        long offset = (page.currentPage() - 1L) * page.pageSize();
        var rows = matching.stream().skip(offset).limit(page.pageSize()).toList();
        var names = ConvertContext.load(rows, Document::reviewerId, reviewers);
        List<DocumentRow> items = mapper.toRows(rows, names);
        return R.success(new PageResult<>(items, matching.size(), page.currentPage(), page.pageSize()));
    }
}
