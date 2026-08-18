package com.librasys.auth.controller;

import com.librasys.auth.dto.MemberResponse;
import com.librasys.auth.model.Member;
import com.librasys.auth.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // GET /api/members/{id}
    @GetMapping("/{id}")
    public MemberResponse getMember(@PathVariable String id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Member not found with id: " + id));
        return new MemberResponse(member.getId(), member.getName(),
                member.getEmail(), member.getRole());
    }

    // PUT /api/members/{id}
    @PutMapping("/{id}")
    public MemberResponse updateMember(@PathVariable String id, @RequestBody Member memberDetails) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Member not found with id: " + id));

        if (memberDetails.getName() != null) {
            member.setName(memberDetails.getName());
        }
        if (memberDetails.getEmail() != null) {
            member.setEmail(memberDetails.getEmail());
        }

        Member saved = memberRepository.save(member);
        return new MemberResponse(saved.getId(), saved.getName(),
                saved.getEmail(), saved.getRole());
    }
}
