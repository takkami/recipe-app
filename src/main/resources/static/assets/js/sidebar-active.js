// サイドバーのアクティブ状態管理

function updateSidebarActiveState() {
    const currentPath = window.location.pathname;
    const sidebarItems = document.querySelectorAll('.sidebar ul li');

    // すべてのアクティブ状態をリセット
    sidebarItems.forEach(item => {
        item.classList.remove('active');
    });

    // 現在のページに応じてアクティブ状態を設定
    sidebarItems.forEach(item => {
        const link = item.querySelector('a');
        const menuItem = item.querySelector('.menu-item');

        if (link) {
            const href = link.getAttribute('href');

            // ホームページの判定
            if ((currentPath === '/home' || currentPath === '/') && href === '/home') {
                item.classList.add('active');
            }
            // お気に入りページの判定
            else if (currentPath === '/recipes/favorites' && href === '/recipes/favorites') {
                item.classList.add('active');
            }
        }

        // メニューアイテム（JavaScript機能）の判定
        if (menuItem) {
            const menuText = menuItem.querySelector('span')?.textContent;

            // カテゴリ一覧が表示されている場合
            if (document.getElementById('categoriesView')?.style.display === 'block' &&
                menuText === 'カテゴリ一覧') {
                item.classList.add('active');
            }
        }
    });

    // レシピフォームページの場合（新規追加・編集）
    if (currentPath.includes('/recipes/new') || currentPath.includes('/recipes/edit')) {
        // 特にアクティブにするメニューはなし（任意で追加可能）
    }
}

// カテゴリページ表示時のアクティブ状態更新（home.htmlでのみ使用）
function showCategoriesPageWithActive() {
    // showCategoriesPage関数が存在する場合のみ実行
    if (typeof showCategoriesPage === 'function') {
        showCategoriesPage();
        updateSidebarActiveState();
    }
}

function hideCategoriesPageWithActive() {
    // hideCategoriesPage関数が存在する場合のみ実行
    if (typeof hideCategoriesPage === 'function') {
        hideCategoriesPage();
        updateSidebarActiveState();
    }
}

// モーダル表示時のアクティブ状態更新
function showUserInfoWithActive() {
    // showUserInfo関数が存在する場合のみ実行
    if (typeof showUserInfo === 'function') {
        showUserInfo();
        // ユーザー情報表示中はサイドバーアクティブ状態を一時的に変更
        document.querySelectorAll('.sidebar ul li').forEach(item => {
            item.classList.remove('active');
            if (item.querySelector('.menu-item span')?.textContent === 'ユーザー情報') {
                item.classList.add('active');
            }
        });
    }
}

function showSettingsWithActive() {
    // showSettings関数が存在する場合のみ実行
    if (typeof showSettings === 'function') {
        showSettings();
        // 設定表示中はサイドバーアクティブ状態を一時的に変更
        document.querySelectorAll('.sidebar ul li').forEach(item => {
            item.classList.remove('active');
            if (item.querySelector('.menu-item span')?.textContent === '設定') {
                item.classList.add('active');
            }
        });
    }
}

function showHelpWithActive() {
    // showHelp関数が存在する場合のみ実行
    if (typeof showHelp === 'function') {
        showHelp();
        // ヘルプ表示中はサイドバーアクティブ状態を一時的に変更
        document.querySelectorAll('.sidebar ul li').forEach(item => {
            item.classList.remove('active');
            if (item.querySelector('.menu-item span')?.textContent === 'ヘルプ') {
                item.classList.add('active');
            }
        });
    }
}

// モーダルが閉じられた時にアクティブ状態を復元
function closeModalWithActive() {
    // closeModal関数が存在する場合のみ実行
    if (typeof closeModal === 'function') {
        closeModal();
        updateSidebarActiveState();
    } else if (typeof closeFormModal === 'function') {
        // recipe_form.htmlの場合
        closeFormModal();
        updateSidebarActiveState();
    }
}

// サイドバーメニューの初期化
function initializeSidebarActiveState() {
    // ページ読み込み時にアクティブ状態を設定
    updateSidebarActiveState();

    // 既存のメニューアイテムのonclickを更新
    const menuItems = document.querySelectorAll('.sidebar .menu-item');
    menuItems.forEach(item => {
        const span = item.querySelector('span');
        if (span) {
            const text = span.textContent;

            switch(text) {
                case 'カテゴリ一覧':
                    // 既存のonclickをアクティブ状態管理付きに置き換え
                    item.onclick = function() {
                        showCategoriesPageWithActive();
                    };
                    break;
                case 'ユーザー情報':
                    item.onclick = function() {
                        showUserInfoWithActive();
                    };
                    break;
                case 'ヘルプ':
                    item.onclick = function() {
                        showHelpWithActive();
                    };
                    break;
                case '設定':
                    item.onclick = function() {
                        showSettingsWithActive();
                    };
                    break;
            }
        }
    });

    // モーダルクローズボタンの更新（存在する場合のみ）
    setTimeout(() => {
        const modalCloseButtons = document.querySelectorAll('.modal-close');
        modalCloseButtons.forEach(button => {
            // 既存のonclickハンドラーを保持
            const originalOnclick = button.onclick;
            button.onclick = function() {
                closeModalWithActive();
            };
        });
    }, 100);
}

// ページのナビゲーション時にアクティブ状態を更新
function handleNavigationActiveState() {
    // ページ遷移時やブラウザの戻る/進むボタンでアクティブ状態を更新
    window.addEventListener('popstate', updateSidebarActiveState);

    // サイドバーのリンククリック時にアクティブ状態を更新
    document.querySelectorAll('.sidebar a').forEach(link => {
        link.addEventListener('click', () => {
            setTimeout(updateSidebarActiveState, 100);
        });
    });
}

// DOMContentLoadedイベントで初期化
document.addEventListener('DOMContentLoaded', function() {
    initializeSidebarActiveState();
    handleNavigationActiveState();
});

// ページの状態変化を監視（SPAの場合）
if (typeof MutationObserver !== 'undefined') {
    const observer = new MutationObserver((mutations) => {
        mutations.forEach((mutation) => {
            // categoriesViewの表示状態変化を監視
            if (mutation.type === 'attributes' &&
                mutation.attributeName === 'style' &&
                mutation.target.id === 'categoriesView') {
                updateSidebarActiveState();
            }
        });
    });

    // カテゴリビューの監視を開始（要素が存在する場合のみ）
    const categoriesView = document.getElementById('categoriesView');
    if (categoriesView) {
        observer.observe(categoriesView, {
            attributes: true,
            attributeFilter: ['style']
        });
    }
}