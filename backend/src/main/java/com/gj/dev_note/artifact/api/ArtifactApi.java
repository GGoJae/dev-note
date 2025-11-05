package com.gj.dev_note.artifact.api;

import com.gj.dev_note.artifact.dto.ArtifactGroupTypeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/artifacts")
@RequiredArgsConstructor
public class ArtifactApi {

    @GetMapping("/group-type-values")
    public List<ArtifactGroupTypeDto.ArtifactGroupValues> getGroupTypeValues() {
        return ArtifactGroupTypeDto.showValues();
    }

}
