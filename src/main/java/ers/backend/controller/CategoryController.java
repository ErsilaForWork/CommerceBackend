package ers.backend.controller;

import ers.backend.DTO.ResponseMessage;
import ers.backend.model.Category;
import ers.backend.service.CateogryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/category")
@CrossOrigin(origins = "http://localhost:5173",
        allowCredentials = "true",
        allowedHeaders = "*"
)
public class CategoryController {

    private final CateogryService cateogryService;

    public CategoryController(CateogryService cateogryService) {
        this.cateogryService = cateogryService;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestParam("name") String categoryName) {

        System.out.println(categoryName);
        if(cateogryService.existsByName(categoryName))
            return new ResponseEntity<>(new ResponseMessage("Category with name "+categoryName+" already exists!"), HttpStatus.BAD_REQUEST);

        Category category = new Category(categoryName);
        return new ResponseEntity<>(cateogryService.save(category), HttpStatus.OK);
    }

    @GetMapping("/all")
    public List<Category> fetchAll() {
        return cateogryService.getAll();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long categoryId) {
        cateogryService.delete(categoryId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> changeName(@PathVariable("id") Long categoryId,@RequestParam("name") String name) {
        Category category;

        try {
            category = cateogryService.getById(categoryId);
        }catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ResponseMessage("No category with id : " + categoryId),HttpStatus.BAD_REQUEST);
        }

        category.setCategoryName(name);
        return new ResponseEntity<>(cateogryService.save(category), HttpStatus.OK);
    }
}
