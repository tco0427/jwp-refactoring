package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.math.BigDecimal;
import java.util.List;
import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @DisplayName("새로운 메뉴를 등록할 수 있다.")
    @Test
    void create() {
        // given
        final MenuProduct menuProduct = new MenuProduct(1L, 1L, 1L);
        final Menu menu = new Menu("후라이드치킨", BigDecimal.valueOf(16000), 2L, List.of(menuProduct));

        // when
        final Menu createdMenu = menuService.create(menu);

        // then
        assertThat(createdMenu).isNotNull();
        assertThat(createdMenu.getId()).isNotNull();
    }

    @DisplayName("메뉴의 가격은 음수일 수 없다.")
    @Test
    void createWithMinusPrice() {
        // given
        final MenuProduct menuProduct = new MenuProduct(1L, 1L, 1L);
        final Menu menu = new Menu("후라이드치킨", BigDecimal.valueOf(-1), 2L, List.of(menuProduct));

        // when & then
        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴의 가격은 반드시 함께 등록되어야 한다.")
    @Test
    void createWithNullPrice() {
        // given
        final MenuProduct menuProduct = new MenuProduct(1L, 1L, 1L);
        final Menu menu = new Menu();
        menu.setName("후라이드치킨");
        menu.setMenuGroupId(2L);
        menu.setMenuProducts(List.of(menuProduct));

        // when & then
        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴는 반드시 어느 메뉴 그룹에 속해있어야 한다.")
    @Test
    void createWithNonGroup() {
        // given
        final MenuProduct menuProduct = new MenuProduct(1L, 1L, 1L);
        final Menu menu = new Menu();
        menu.setName("후라이드치킨");
        menu.setPrice(BigDecimal.valueOf(16000));
        menu.setMenuProducts(List.of(menuProduct));

        // when & then
        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("메뉴의 가격이 상품(product)의 금액 총합(가격 * 수량) 보다 크면 안된다.")
    @Test
    void createWithLessPriceThenTotalProductPrice() {
        // given
        final MenuProduct menuProduct = new MenuProduct(1L, 1L, 1L);
        final Menu menu = new Menu("후라이드치킨", BigDecimal.valueOf(16001), 2L, List.of(menuProduct));

        // when & then
        assertThatThrownBy(() -> menuService.create(menu))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("전체 메뉴 리스트를 조회할 수 있다.")
    @Test
    void list() {
        // when
        final List<Menu> menus = menuService.list();

        // then
        assertThat(menus)
                .hasSize(6)
                .extracting("name", "price", "menuGroupId")
                .containsExactlyInAnyOrder(
                        tuple("후라이드치킨", BigDecimal.valueOf(16000), 2L),
                        tuple("양념치킨", BigDecimal.valueOf(16000), 2L),
                        tuple("반반치킨", BigDecimal.valueOf(16000), 2L),
                        tuple("통구이", BigDecimal.valueOf(16000), 2L),
                        tuple("간장치킨", BigDecimal.valueOf(17000), 2L),
                        tuple("순살치킨", BigDecimal.valueOf(17000), 2L)
                );
    }
}
