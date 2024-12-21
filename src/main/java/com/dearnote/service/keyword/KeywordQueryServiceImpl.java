package com.dearnote.service.keyword;

import com.dearnote.apipayload.code.status.ErrorStatus;
import com.dearnote.apipayload.exception.handler.MemberHandler;
import com.dearnote.domain.Keyword;
import com.dearnote.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordQueryServiceImpl implements KeywordQueryService {

    private final KeywordRepository keywordRepository;

    @Override
    public Keyword getKeyword(Long keywordId) {
        return keywordRepository.findById(keywordId)
                .orElseThrow(() -> new MemberHandler(ErrorStatus.KEYWORD_NOT_FOUND));
    }

    @Override
    public Keyword getRandomKeyword(){
        List<Keyword> allKeywords = keywordRepository.findAll();
        Collections.shuffle(allKeywords);
        return allKeywords.get(0);
    }
}
