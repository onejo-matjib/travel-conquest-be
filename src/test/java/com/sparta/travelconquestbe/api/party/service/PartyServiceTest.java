package com.sparta.travelconquestbe.api.party.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sparta.travelconquestbe.api.party.dto.request.PartyCreateRequest;
import com.sparta.travelconquestbe.api.party.dto.response.PartyCreateResponse;
import com.sparta.travelconquestbe.common.auth.AuthUserInfo;
import com.sparta.travelconquestbe.domain.PartyTag.entity.PartyTag;
import com.sparta.travelconquestbe.domain.PartyTag.repository.PartyTagRepository;
import com.sparta.travelconquestbe.domain.party.entity.Party;
import com.sparta.travelconquestbe.domain.party.repository.PartyRepository;
import com.sparta.travelconquestbe.domain.partyMember.entity.PartyMember;
import com.sparta.travelconquestbe.domain.partyMember.repository.PartyMemberRepository;
import com.sparta.travelconquestbe.domain.tag.entity.Tag;
import com.sparta.travelconquestbe.domain.tag.repository.TagRepository;
import com.sparta.travelconquestbe.domain.user.entity.User;
import com.sparta.travelconquestbe.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PartyServiceTest {

  @InjectMocks
  private PartyService partyService;

  @Mock
  private PartyRepository partyRepository;

  @Mock
  private TagRepository tagRepository;

  @Mock
  private PartyMemberRepository partyMemberRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PartyTagRepository partyTagRepository;

  private AuthUserInfo userInfo;

  @BeforeEach
  public void setup() {
    userInfo = new AuthUserInfo(1L, "John Doe", "johnny", "john@example.com", "google",
        "1990-01-01", null, null);
  }

  @Test
  public void testCreateParty_Success() {
    // given
    PartyCreateRequest request = new PartyCreateRequest("Party Name",
        "Party Description",
        10,
        false,
        null,
        "#tag1 #tag2"
    );

    when(userRepository.getReferenceById(userInfo.getId())).thenReturn(
        User.builder().id(1L).build());
    when(partyRepository.save(any(Party.class))).thenAnswer(
        invocation -> invocation.getArgument(0));
    when(tagRepository.findByKeyword(anyString())).thenReturn(Optional.empty());
    when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    PartyCreateResponse response = partyService.createParty(userInfo, request);

    // then
    assertThat(response.getName()).isEqualTo(request.getName());
    assertThat(response.getDescription()).isEqualTo(request.getDescription());
    assertThat(response.getTags()).containsExactlyInAnyOrder("tag1", "tag2");

    verify(partyRepository, times(1)).save(any(Party.class));
    verify(partyMemberRepository, times(1)).save(any(PartyMember.class));
    verify(tagRepository, times(2)).save(any(Tag.class));
    verify(partyTagRepository, times(2)).save(any(PartyTag.class));
  }

  @Test
  public void testCreateParty_WithPassword() {
    // given
    PartyCreateRequest request = new PartyCreateRequest("Party Name", "Party Description", 10, true,
        "password123", "#tag1 #tag2");

    when(userRepository.getReferenceById(userInfo.getId())).thenReturn(
        User.builder().id(1L).build());
    when(partyRepository.save(any(Party.class))).thenAnswer(
        invocation -> invocation.getArgument(0));

    // when
    PartyCreateResponse response = partyService.createParty(userInfo, request);

    // then
    assertThat(response.isPasswordStatus()).isTrue();
    assertThat(response.getPassword()).isEqualTo(request.getPassword());

    verify(partyRepository, times(1)).save(any(Party.class));
    verify(partyMemberRepository, times(1)).save(any(PartyMember.class));
  }

  @Test
  public void testExtractHashtags() {
    // given
    String tags = "#hello #world  #java!";

    // when
    var result = PartyService.extractHashtags(tags);

    // then
    assertThat(result).containsExactly("hello", "world", "java");
  }
}