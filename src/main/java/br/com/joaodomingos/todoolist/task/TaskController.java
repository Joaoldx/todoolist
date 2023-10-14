package br.com.joaodomingos.todoolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.joaodomingos.todoolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest servletRequest) {
        var idUser = servletRequest.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);
        
        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt())) {
            if (currentDate.isAfter(taskModel.getEndAt())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de termino deve ser maior que a data atual");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser maior que a data atual");
        }
        
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início deve ser menor de termino");
        }
        
        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }
    
    public ResponseEntity<List<TaskModel>> list(HttpServletRequest servletRequest) {
        var idUser = servletRequest.getAttribute("idUser");
        var tasks = this.taskRepository.findByIdUser((UUID) idUser);
        return ResponseEntity.status(HttpStatus.OK).body(tasks);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<TaskModel> update (@RequestBody TaskModel taskModel, HttpServletRequest servletRequest, @PathVariable UUID id) {        
        var task = this.taskRepository.findById(id).orElse(null);

        Utils.copyNonNullProperties(taskModel, task);

        this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete (HttpServletRequest servletRequest, @PathVariable UUID id) {
        this.taskRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("Removido com sucesso");
    }
}
