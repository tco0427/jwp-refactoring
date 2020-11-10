package kitchenpos.dao;

import kitchenpos.domain.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    @Query("select DISTINCT m from Menu m join fetch m.menuGroup g join fetch m.menuProducts")
    List<Menu> findAllFetch();

    int countAllByIdIn(List<Long> ids);
}