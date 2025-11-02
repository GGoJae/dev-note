package com.gj.dev_note.note.authz;

import com.gj.dev_note.note.repository.NoteRepository;
import com.gj.dev_note.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("noteAuthz")
@RequiredArgsConstructor
public class NoteAuthz {

    private final NoteRepository noteRepo;

    public boolean isOwner(Long noteId) {
        Long me = CurrentUser.id();
        return noteRepo.existsByIdAndOwnerId(noteId, me);
    }
}
