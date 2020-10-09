package io.sharpink.persistence.entity.member;

import java.util.List;
import java.util.Optional;

import javax.persistence.*;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import io.sharpink.persistence.entity.story.Story;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected Long id;

	// Informations principales, obligatoires

	protected String nickname;

	protected String email;

	protected String password;

	// Informations complémentaires, optionnelles. Suppression du getter généré par
	// Lombok pour utiliser un getter spécial (voir plus bas)
	@OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@Getter(AccessLevel.NONE)
	protected MemberDetails memberDetails;

	// Les histoires du membre

	/**
	 * Permet d'avoir toujours l'information du nombre d'histoires d'un membre, sans
	 * avoir à forcément récupérer les Story, qui seront <i>lazily loaded</i> par
	 * Hibernate
	 */
	@Column(name = "STORIES_COUNT")
	protected Long storiesCount;

	@OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
	@LazyCollection(LazyCollectionOption.TRUE)
	protected List<Story> stories;

	// getter personnalisé pour renvoyer un Optional<MemberDetails> au lieu d'un MemberDetails
	public Optional<MemberDetails> getMemberDetails() {
		return Optional.ofNullable(memberDetails);
	}

}
