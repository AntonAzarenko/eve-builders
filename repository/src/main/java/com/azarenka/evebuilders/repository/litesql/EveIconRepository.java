package com.azarenka.evebuilders.repository.litesql;

import com.azarenka.evebuilders.domain.sqllite.EveIcon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EveIconRepository extends JpaRepository<EveIcon, Integer> {

    List<EveIcon> findByIconIdIn(List<Integer> iconIds);

    EveIcon findByIconId(Integer iconId);
}
