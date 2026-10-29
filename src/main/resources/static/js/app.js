/**
 * =============================================================================
 * LOGIC ĐIỀU HƯỚNG & XỬ LÝ 3 MÀN HÌNH CHÍNH (LUỒNG L5)
 * Khớp 100% Wireframe M1, M2, M3 & Tiêu chuẩn S4:
 * Xử lý đầy đủ 3 trạng thái: Đang tải (Loading), Rỗng (Empty), Lỗi kết nối (Error)
 * =============================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

function initApp() {
    setupAuth();
    setupNavigation();
    setupCenterSelector();
    setupStockScreen();
    setupExportScreen();
    setupTransactionsScreen();
    setupModals();

    // Tải dữ liệu màn hình đầu tiên (M1 - Tồn kho)
    loadStockData();
}

// =============================================================================
// XÁC THỰC ĐĂNG NHẬP CƠ BẢN (THÀNH PHẦN 3 - TRACK SE)
// =============================================================================
function setupAuth() {
    updateAuthUI();

    const btnLoginTrigger = document.getElementById('btn-login-trigger');
    if (btnLoginTrigger) {
        btnLoginTrigger.addEventListener('click', () => openModal('modal-login'));
    }

    const btnLogout = document.getElementById('btn-logout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            window.apiClient.logout();
            showToast('Đã đăng xuất khỏi hệ thống', 'info');
            updateAuthUI();
            openModal('modal-login');
        });
    }

    const btnQuickKtv = document.getElementById('btn-quick-login-ktv');
    if (btnQuickKtv) {
        btnQuickKtv.addEventListener('click', () => {
            document.getElementById('login-username').value = 'ktv_trong';
            document.getElementById('login-password').value = '123456';
        });
    }

    const btnQuickManager = document.getElementById('btn-quick-login-manager');
    if (btnQuickManager) {
        btnQuickManager.addEventListener('click', () => {
            document.getElementById('login-username').value = 'quanly_kho';
            document.getElementById('login-password').value = '123456';
        });
    }

    const formLogin = document.getElementById('form-login');
    if (formLogin) {
        formLogin.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('login-username').value.trim();
            const password = document.getElementById('login-password').value;
            const errorDiv = document.getElementById('login-error-msg');
            const submitBtn = document.getElementById('btn-submit-login');

            if (errorDiv) errorDiv.style.display = 'none';
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Đang xác thực...';
            }

            try {
                const user = await window.apiClient.login(username, password);
                closeModal('modal-login');
                showToast(`✓ Chào mừng ${user.fullName} (${user.role === 'TECHNICIAN' ? 'Kỹ thuật viên' : 'Quản lý kho'})!`, 'success');
                updateAuthUI();

                // Đồng bộ chi nhánh nếu có
                if (user.centerId) {
                    const centerSelect = document.getElementById('select-center');
                    if (centerSelect) centerSelect.value = user.centerId.toString();
                    loadStockData();
                }
            } catch (err) {
                if (errorDiv) {
                    errorDiv.textContent = `Lỗi: ${err.message}`;
                    errorDiv.style.display = 'block';
                }
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = '✓ Xác nhận Đăng nhập';
                }
            }
        });
    }

    // Nếu chưa đăng nhập, tự động bật modal đăng nhập
    if (!window.apiClient.isAuthenticated()) {
        setTimeout(() => openModal('modal-login'), 500);
    }
}

function updateAuthUI() {
    const user = window.apiClient.getCurrentUser();
    const avatarEl = document.getElementById('topbar-avatar');
    const nameEl = document.getElementById('topbar-username');
    const roleEl = document.getElementById('topbar-role');
    const btnLogin = document.getElementById('btn-login-trigger');
    const btnLogout = document.getElementById('btn-logout');
    const exportPerformedBy = document.getElementById('export-performed-by');
    const btnQuickImport = document.getElementById('btn-quick-import');
    const btnCreatePart = document.getElementById('btn-create-part');
    const btnSubmitExport = document.getElementById('btn-submit-export');
    const exportRbacWarning = document.getElementById('export-rbac-warning');

    if (user && window.apiClient.isAuthenticated()) {
        if (avatarEl) avatarEl.textContent = user.username ? user.username.substring(0, 2).toUpperCase() : 'US';
        if (nameEl) nameEl.textContent = user.fullName || user.username;
        if (roleEl) roleEl.textContent = user.role === 'TECHNICIAN' ? 'Kỹ thuật viên' : 'Quản lý kho';
        if (btnLogin) btnLogin.style.display = 'none';
        if (btnLogout) btnLogout.style.display = 'inline-block';
        if (exportPerformedBy) {
            exportPerformedBy.value = `${user.fullName} (${user.username})`;
            exportPerformedBy.readOnly = true;
        }

        const exportManagerBlock = document.getElementById('export-manager-block');
        const formExportPart = document.getElementById('form-export-part');

        // Áp dụng Ma trận Phân quyền RBAC (api-contract.md Mục 2.2 & QT-14)
        if (user.role === 'TECHNICIAN') {
            // Kỹ thuật viên: KHÔNG ĐƯỢC Nhập kho / Thêm linh kiện (ẩn nút), ĐƯỢC Xuất kho
            if (btnQuickImport) btnQuickImport.style.display = 'none';
            if (btnCreatePart) btnCreatePart.style.display = 'none';
            if (btnSubmitExport) {
                btnSubmitExport.disabled = false;
                btnSubmitExport.innerHTML = '✓ Xác nhận xuất kho';
            }
            if (exportManagerBlock) exportManagerBlock.style.display = 'none';
            if (formExportPart) formExportPart.style.display = 'block';
        } else if (user.role === 'MANAGER') {
            // Quản lý kho: ĐƯỢC Nhập kho / Thêm linh kiện (hiện nút), KHÔNG ĐƯỢC tự ý Xuất kho sửa chữa
            if (btnQuickImport) btnQuickImport.style.display = 'inline-flex';
            if (btnCreatePart) btnCreatePart.style.display = 'inline-flex';
            if (btnSubmitExport) {
                btnSubmitExport.disabled = true;
                btnSubmitExport.innerHTML = '🔒 Chỉ Kỹ thuật viên được xuất kho';
            }
            if (exportManagerBlock) exportManagerBlock.style.display = 'block';
            if (formExportPart) formExportPart.style.display = 'none';
        }
    } else {
        if (avatarEl) avatarEl.textContent = 'G';
        if (nameEl) nameEl.textContent = 'Chưa đăng nhập';
        if (roleEl) roleEl.textContent = 'Guest';
        if (btnLogin) btnLogin.style.display = 'inline-block';
        if (btnLogout) btnLogout.style.display = 'none';
        if (btnQuickImport) btnQuickImport.style.display = 'none';
        if (btnCreatePart) btnCreatePart.style.display = 'none';
        if (btnSubmitExport) {
            btnSubmitExport.disabled = true;
            btnSubmitExport.innerHTML = '🔒 Đăng nhập để thao tác';
        }
        const exportManagerBlock = document.getElementById('export-manager-block');
        const formExportPart = document.getElementById('form-export-part');
        if (exportManagerBlock) exportManagerBlock.style.display = 'none';
        if (formExportPart) formExportPart.style.display = 'block';
        if (exportPerformedBy) {
            exportPerformedBy.value = 'Chưa đăng nhập';
            exportPerformedBy.readOnly = false;
        }
    }

    // Cập nhật lại các nút hành động trong bảng tồn kho nếu đang hiển thị
    const tableContainer = document.getElementById('stock-table-content');
    if (tableContainer && stockState.items && stockState.items.length > 0) {
        renderStockTable(tableContainer, stockState.items);
    }
}

// =============================================================================
// ĐIỀU HƯỚNG 3 MÀN HÌNH (M1, M2, M3)
// =============================================================================
function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    const screenViews = document.querySelectorAll('.screen-view');
    const pageTitle = document.getElementById('page-title');

    const titles = {
        'screen-stock': 'Bảng Điều Khiển Tồn Kho & Cảnh Báo (M1)',
        'screen-export': 'Phiếu Xuất Linh Kiện Cho Phiếu Bảo Hành (M2)',
        'screen-transactions': 'Sổ Cái Lịch Sử Giao Dịch Kho (M3)'
    };

    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const targetScreenId = item.getAttribute('data-screen');

            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');

            screenViews.forEach(view => {
                view.classList.remove('active');
                if (view.id === targetScreenId) {
                    view.classList.add('active');
                }
            });

            if (pageTitle && titles[targetScreenId]) {
                pageTitle.textContent = titles[targetScreenId];
            }

            // Tải dữ liệu tương ứng khi chuyển màn hình
            if (targetScreenId === 'screen-stock') {
                loadStockData();
            } else if (targetScreenId === 'screen-transactions') {
                loadTransactionsData();
            } else if (targetScreenId === 'screen-export') {
                const currentUser = window.apiClient.getCurrentUser();
                if (currentUser && currentUser.role === 'MANAGER') {
                    showToast('🔒 Thông báo phân quyền: Quản lý kho không có quyền lập phiếu xuất kho linh kiện.', 'warning');
                }
                initExportForm();
            }
        });
    });
}

function setupCenterSelector() {
    const select = document.getElementById('select-center');
    if (!select) return;

    select.addEventListener('change', () => {
        const centerId = Number(select.value);
        window.apiClient.setCenterId(centerId);
        showToast(`Đã chuyển sang Trung tâm Bảo hành ${select.options[select.selectedIndex].text}`, 'info');

        // Tải lại màn hình đang mở
        const activeScreen = document.querySelector('.screen-view.active');
        if (activeScreen && activeScreen.id === 'screen-stock') {
            loadStockData();
        } else if (activeScreen && activeScreen.id === 'screen-transactions') {
            loadTransactionsData();
        } else if (activeScreen && activeScreen.id === 'screen-export') {
            updateStockPreviewForExport();
        }
    });
}

// =============================================================================
// MÀN HÌNH 1 (M1) — TRA CỨU TỒN KHO & CẢNH BÁO TỒN THẤP
// =============================================================================
let stockState = {
    keyword: '',
    isLowStock: false,
    page: 1,
    limit: 15,
    totalPages: 1,
    totalItems: 0,
    items: []
};

function setupStockScreen() {
    const searchInput = document.getElementById('stock-search-input');
    const lowStockCheckbox = document.getElementById('checkbox-low-stock');
    const btnPrev = document.getElementById('stock-btn-prev');
    const btnNext = document.getElementById('stock-btn-next');
    const btnRefresh = document.getElementById('btn-refresh-stock');
    const btnCreatePart = document.getElementById('btn-create-part');
    const btnQuickImport = document.getElementById('btn-quick-import');

    let debounceTimer = null;
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                stockState.keyword = e.target.value;
                stockState.page = 1;
                loadStockData();
            }, 300);
        });
    }

    if (lowStockCheckbox) {
        lowStockCheckbox.addEventListener('change', (e) => {
            stockState.isLowStock = e.target.checked;
            stockState.page = 1;
            loadStockData();
        });
    }

    if (btnPrev) {
        btnPrev.addEventListener('click', () => {
            if (stockState.page > 1) {
                stockState.page--;
                loadStockData();
            }
        });
    }

    if (btnNext) {
        btnNext.addEventListener('click', () => {
            if (stockState.page < stockState.totalPages) {
                stockState.page++;
                loadStockData();
            }
        });
    }

    if (btnRefresh) {
        btnRefresh.addEventListener('click', () => loadStockData());
    }

    if (btnCreatePart) {
        btnCreatePart.addEventListener('click', () => openCreatePartModal());
    }

    if (btnQuickImport) {
        btnQuickImport.addEventListener('click', () => openImportModal());
    }
}

async function loadStockData() {
    const container = document.getElementById('stock-table-content');
    const pagination = document.getElementById('stock-pagination');
    if (!container) return;

    // 1. TRẠNG THÁI ĐANG TẢI (LOADING STATE)
    renderLoadingState(container, 'Đang tải dữ liệu tồn kho từ máy chủ...');
    if (pagination) pagination.style.display = 'none';

    try {
        const response = await window.apiClient.getStockList({
            keyword: stockState.keyword,
            isLowStock: stockState.isLowStock,
            page: stockState.page,
            limit: stockState.limit
        });

        const items = response.data || [];
        const meta = response.meta || { page: 1, total: items.length, totalPages: 1 };

        stockState.items = items;
        stockState.totalPages = meta.totalPages || 1;
        stockState.totalItems = meta.total || 0;

        // Cập nhật thẻ chỉ số thống kê (KPIs)
        updateStockMetrics(items, meta.total);

        // 2. TRẠNG THÁI RỖNG (EMPTY STATE)
        if (items.length === 0) {
            const isFilter = stockState.keyword || stockState.isLowStock;
            renderEmptyState(
                container,
                isFilter ? 'Không tìm thấy linh kiện nào khớp với điều kiện tìm kiếm' : 'Kho trung tâm này hiện chưa có dữ liệu tồn kho',
                isFilter ? 'Thử tìm với từ khóa khác hoặc bỏ chọn lọc cảnh báo tồn thấp.' : 'Nhấn nút "Nhập kho" để thêm linh kiện đầu tiên.',
                isFilter ? () => {
                    const searchInput = document.getElementById('stock-search-input');
                    const lowStockCheckbox = document.getElementById('checkbox-low-stock');
                    if (searchInput) searchInput.value = '';
                    if (lowStockCheckbox) lowStockCheckbox.checked = false;
                    stockState.keyword = '';
                    stockState.isLowStock = false;
                    stockState.page = 1;
                    loadStockData();
                } : null,
                isFilter ? 'Xóa bộ lọc' : null
            );
            return;
        }

        // 3. TRẠNG THÁI CÓ DỮ LIỆU (SUCCESS STATE)
        renderStockTable(container, items);

        if (pagination) {
            pagination.style.display = 'flex';
            document.getElementById('stock-page-info').textContent = 
                `Hiển thị ${(meta.page - 1) * meta.limit + 1} - ${Math.min(meta.page * meta.limit, meta.total)} trên tổng số ${meta.total} linh kiện`;
            
            const btnPrev = document.getElementById('stock-btn-prev');
            const btnNext = document.getElementById('stock-btn-next');
            if (btnPrev) btnPrev.disabled = (stockState.page <= 1);
            if (btnNext) btnNext.disabled = (stockState.page >= stockState.totalPages);
        }

    } catch (err) {
        // 4. TRẠNG THÁI LỖI KẾT NỐI (ERROR STATE)
        renderErrorState(container, err.message, () => loadStockData());
    }
}

function updateStockMetrics(currentItems, totalCount) {
    const totalEl = document.getElementById('metric-total-parts');
    const lowStockEl = document.getElementById('metric-low-stock');
    const outStockEl = document.getElementById('metric-out-stock');
    const inStockEl = document.getElementById('metric-in-stock');
    const rateEl = document.getElementById('metric-in-stock-rate');

    const total = totalCount || currentItems.length || 0;
    if (totalEl) totalEl.textContent = total;

    let lowCount = 0;
    let outCount = 0;
    currentItems.forEach(item => {
        if (item.quantity === 0) outCount++;
        else if (item.isLowStock) lowCount++;
    });

    const inStockCount = Math.max(0, currentItems.length - lowCount - outCount);
    if (lowStockEl) lowStockEl.textContent = lowCount;
    if (outStockEl) outStockEl.textContent = outCount;
    if (inStockEl) inStockEl.textContent = inStockCount;
}

function renderStockTable(container, items) {
    let html = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>Linh kiện</th>
                    <th>Danh mục</th>
                    <th>Số lượng tồn</th>
                    <th>Ngưỡng cảnh báo</th>
                    <th>Trạng thái</th>
                    <th>Đơn giá</th>
                    <th>Thao tác</th>
                </tr>
            </thead>
            <tbody>
    `;

    const currentUser = window.apiClient.getCurrentUser();
    const isManager = (currentUser && currentUser.role === 'MANAGER');

    items.forEach(item => {
        let statusBadge = '<span class="badge badge-success">✓ Đủ hàng</span>';
        if (item.quantity === 0) {
            statusBadge = '<span class="badge badge-danger">✕ Hết hàng</span>';
        } else if (item.isLowStock) {
            statusBadge = '<span class="badge badge-warning">⚠ Cảnh báo tồn thấp</span>';
        }

        const formattedPrice = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.unitPrice || 0);

        const actionBtn = isManager 
            ? `<div class="btn-actions-group">
                <button class="btn btn-secondary btn-sm" title="Chỉnh sửa thông tin linh kiện" onclick="openEditPartModal('${escapeHtml(item.partId)}', '${escapeHtml(item.partName)}', '${escapeHtml(item.category || 'MAN_HINH')}', '${escapeHtml(item.unit || 'Cái')}', ${item.unitPrice || 0})">
                    ✏ Sửa
                </button>
                <button class="btn btn-secondary btn-sm" title="Điều chỉnh định mức cảnh báo tồn" onclick="openThresholdModal('${escapeHtml(item.partId)}', ${item.minThreshold}, '${escapeHtml(item.partName)}')">
                    ⚙ Ngưỡng
                </button>
                <button class="btn btn-danger-outline btn-sm" title="Xóa linh kiện khỏi danh mục" onclick="deletePartAction('${escapeHtml(item.partId)}', '${escapeHtml(item.partName)}')">
                    🗑 Xóa
                </button>
               </div>`
            : `<button class="btn btn-secondary btn-sm" disabled style="opacity: 0.5; cursor: not-allowed;" title="Chỉ Quản lý kho mới được quyền thao tác CRUD">
                🔒 Chỉ Quản lý
               </button>`;

        html += `
            <tr>
                <td>
                    <div class="part-cell">
                        <span class="part-name">${escapeHtml(item.partName)}</span>
                        <span class="part-code">${escapeHtml(item.partId)}</span>
                    </div>
                </td>
                <td><span class="badge badge-cyan">${escapeHtml(item.category || 'LINH_KIEN')}</span></td>
                <td>
                    <strong style="font-size: 15px; color: ${item.isLowStock ? 'var(--warning)' : 'var(--text-main)'};">
                        ${item.quantity} ${escapeHtml(item.unit || 'Cái')}
                    </strong>
                </td>
                <td>
                    <span style="color: var(--text-dim); font-weight: 500;">≥ ${item.minThreshold}</span>
                </td>
                <td>${statusBadge}</td>
                <td>${formattedPrice}</td>
                <td>
                    ${actionBtn}
                </td>
            </tr>
        `;
    });

    html += `</tbody></table>`;
    container.innerHTML = html;
}

// =============================================================================
// MÀN HÌNH 2 (M2) — PHIẾU XUẤT KHO CHO PHIẾU BẢO HÀNH
// =============================================================================
function setupExportScreen() {
    const form = document.getElementById('form-export-part');
    const selectPart = document.getElementById('export-part-id');
    const inputQty = document.getElementById('export-quantity');

    if (selectPart) {
        selectPart.addEventListener('change', () => updateStockPreviewForExport());
    }

    if (inputQty) {
        inputQty.addEventListener('input', () => validateExportQty());
    }

    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            await handleExportSubmit(form);
        });
    }

    // Nút chuyển nhanh sang KTV trong khối thông báo phân quyền của Quản lý kho
    const btnSwitchKtv = document.getElementById('btn-switch-to-ktv');
    if (btnSwitchKtv) {
        btnSwitchKtv.addEventListener('click', async () => {
            try {
                showToast('Đang chuyển đổi sang tài khoản Kỹ thuật viên...', 'info');
                await window.apiClient.login('ktv_trong', '123456');
                updateAuthUI();
                showToast('✓ Đã chuyển sang tài khoản Kỹ thuật viên (Phạm Trí Trọng)!', 'success');
                initExportForm();
            } catch (err) {
                showToast('Lỗi chuyển đổi tài khoản: ' + err.message, 'error');
            }
        });
    }

    // Nút quay về Bảng tồn kho M1
    const btnBackToStock = document.getElementById('btn-back-to-stock');
    if (btnBackToStock) {
        btnBackToStock.addEventListener('click', () => {
            const navStock = document.getElementById('nav-stock');
            if (navStock) navStock.click();
        });
    }
}

async function initExportForm() {
    const selectPart = document.getElementById('export-part-id');
    if (!selectPart) return;

    selectPart.innerHTML = '<option value="">-- Đang tải danh sách linh kiện... --</option>';

    try {
        const response = await window.apiClient.getStockList({ limit: 100 });
        const items = response.data || [];

        if (items.length === 0) {
            selectPart.innerHTML = '<option value="">-- Kho hiện chưa có linh kiện --</option>';
            return;
        }

        let options = '<option value="">-- Chọn linh kiện cần xuất --</option>';
        items.forEach(item => {
            options += `<option value="${escapeHtml(item.partId)}" data-stock="${item.quantity}">
                ${escapeHtml(item.partId)} — ${escapeHtml(item.partName)} (Tồn: ${item.quantity})
            </option>`;
        });
        selectPart.innerHTML = options;

        updateStockPreviewForExport();
    } catch (err) {
        selectPart.innerHTML = '<option value="">-- Lỗi tải danh sách linh kiện --</option>';
        showToast('Không thể tải danh sách tồn kho cho form xuất', 'error');
    }
}

function updateStockPreviewForExport() {
    const selectPart = document.getElementById('export-part-id');
    const previewEl = document.getElementById('export-stock-preview');
    if (!selectPart || !previewEl) return;

    const selectedOption = selectPart.options[selectPart.selectedIndex];
    if (!selectedOption || !selectedOption.value) {
        previewEl.textContent = 'Chọn linh kiện để kiểm tra số tồn khả dụng tức thời.';
        previewEl.style.color = 'var(--text-dim)';
        return;
    }

    const currentStock = Number(selectedOption.getAttribute('data-stock') || 0);
    if (currentStock === 0) {
        previewEl.innerHTML = `⚠️ <strong style="color: var(--danger)">Hết hàng trong kho (Tồn: 0)!</strong> Không thể xuất.`;
    } else {
        previewEl.innerHTML = `✓ Số lượng tồn khả dụng tại kho: <strong style="color: var(--cyan)">${currentStock}</strong> cái`;
    }
    validateExportQty();
}

function validateExportQty() {
    const selectPart = document.getElementById('export-part-id');
    const inputQty = document.getElementById('export-quantity');
    const warningEl = document.getElementById('export-qty-warning');
    if (!selectPart || !inputQty || !warningEl) return;

    const selectedOption = selectPart.options[selectPart.selectedIndex];
    if (!selectedOption || !selectedOption.value) {
        warningEl.textContent = '';
        return;
    }

    const currentStock = Number(selectedOption.getAttribute('data-stock') || 0);
    const requestedQty = Number(inputQty.value || 0);

    if (requestedQty > currentStock) {
        warningEl.textContent = `❌ Số lượng xuất (${requestedQty}) vượt quá tồn kho khả dụng (${currentStock})!`;
        warningEl.style.color = 'var(--danger)';
    } else {
        warningEl.textContent = '';
    }
}

async function handleExportSubmit(form) {
    const btnSubmit = document.getElementById('btn-submit-export');
    const partId = document.getElementById('export-part-id').value;
    const ticketId = document.getElementById('export-ticket-id').value;
    const quantity = document.getElementById('export-quantity').value;
    const performedBy = document.getElementById('export-performed-by').value;
    const note = document.getElementById('export-note').value;

    if (!partId || !ticketId || !quantity || !performedBy) {
        showToast('Vui lòng điền đầy đủ các trường bắt buộc', 'warning');
        return;
    }

    if (btnSubmit) {
        btnSubmit.disabled = true;
        btnSubmit.innerHTML = '<span class="spinner" style="width:16px;height:16px;border-width:2px;display:inline-block"></span> Đang xử lý...';
    }

    try {
        const response = await window.apiClient.exportPart({
            partId,
            ticketId,
            quantity,
            performedBy,
            note
        });

        const tx = response.data;
        showToast(`✓ Xuất kho thành công! Mã giao dịch: ${tx.transactionId}. Số tồn còn lại: ${tx.remainingStock}`, 'success');

        // Reset form
        form.reset();
        document.getElementById('export-performed-by').value = 'KTV - Phạm Trí Trọng';
        initExportForm();

    } catch (err) {
        showToast(`Lỗi xuất kho: ${err.message}`, 'error');
    } finally {
        if (btnSubmit) {
            btnSubmit.disabled = false;
            btnSubmit.innerHTML = '✓ Xác nhận xuất kho';
        }
    }
}

// =============================================================================
// MÀN HÌNH 3 (M3) — SỔ CÁI LỊCH SỬ GIAO DỊCH KHO (APPEND-ONLY LOG)
// =============================================================================
let txState = {
    partId: '',
    type: '',
    fromDate: '',
    toDate: '',
    page: 1,
    limit: 15,
    totalPages: 1
};

function setupTransactionsScreen() {
    const selectType = document.getElementById('tx-filter-type');
    const inputPart = document.getElementById('tx-filter-part');
    const btnFilter = document.getElementById('tx-btn-apply-filter');
    const btnPrev = document.getElementById('tx-btn-prev');
    const btnNext = document.getElementById('tx-btn-next');

    if (btnFilter) {
        btnFilter.addEventListener('click', () => {
            if (selectType) txState.type = selectType.value;
            if (inputPart) txState.partId = inputPart.value;
            txState.page = 1;
            loadTransactionsData();
        });
    }

    if (btnPrev) {
        btnPrev.addEventListener('click', () => {
            if (txState.page > 1) {
                txState.page--;
                loadTransactionsData();
            }
        });
    }

    if (btnNext) {
        btnNext.addEventListener('click', () => {
            if (txState.page < txState.totalPages) {
                txState.page++;
                loadTransactionsData();
            }
        });
    }
}

async function loadTransactionsData() {
    const container = document.getElementById('tx-table-content');
    const pagination = document.getElementById('tx-pagination');
    if (!container) return;

    renderLoadingState(container, 'Đang tải sổ cái lịch sử giao dịch...');
    if (pagination) pagination.style.display = 'none';

    try {
        const response = await window.apiClient.getTransactions({
            partId: txState.partId,
            type: txState.type,
            fromDate: txState.fromDate,
            toDate: txState.toDate,
            page: txState.page,
            limit: txState.limit
        });

        const items = response.data || [];
        const meta = response.meta || { page: 1, limit: txState.limit, total: items.length, totalPages: 1 };

        txState.totalPages = meta.totalPages || 1;

        if (items.length === 0) {
            renderEmptyState(
                container,
                'Chưa có giao dịch nào khớp với bộ lọc',
                'Thử thay đổi loại giao dịch hoặc mã linh kiện để tra cứu.',
                () => {
                    const inputPart = document.getElementById('tx-filter-part');
                    const selectType = document.getElementById('tx-filter-type');
                    if (inputPart) inputPart.value = '';
                    if (selectType) selectType.value = '';
                    txState.partId = '';
                    txState.type = '';
                    txState.page = 1;
                    loadTransactionsData();
                },
                'Xóa bộ lọc'
            );
            return;
        }

        renderTransactionsTable(container, items);

        if (pagination) {
            pagination.style.display = 'flex';
            document.getElementById('tx-page-info').textContent = 
                `Hiển thị ${(meta.page - 1) * meta.limit + 1} - ${Math.min(meta.page * meta.limit, meta.total)} trên tổng số ${meta.total} giao dịch`;
            
            const btnPrev = document.getElementById('tx-btn-prev');
            const btnNext = document.getElementById('tx-btn-next');
            if (btnPrev) btnPrev.disabled = (txState.page <= 1);
            if (btnNext) btnNext.disabled = (txState.page >= txState.totalPages);
        }

    } catch (err) {
        renderErrorState(container, err.message, () => loadTransactionsData());
    }
}

function renderTransactionsTable(container, items) {
    let html = `
        <table class="data-table">
            <thead>
                <tr>
                    <th>Mã giao dịch</th>
                    <th>Thời điểm</th>
                    <th>Loại</th>
                    <th>Linh kiện</th>
                    <th>Số lượng</th>
                    <th>Phiếu bảo hành</th>
                    <th>Người thực hiện</th>
                    <th>Ghi chú</th>
                </tr>
            </thead>
            <tbody>
    `;

    items.forEach(item => {
        const isImport = (item.transactionType === 'IMPORT');
        const badge = isImport 
            ? '<span class="badge badge-success">⬇ Nhập kho</span>' 
            : '<span class="badge badge-purple">⬆ Xuất kho</span>';

        html += `
            <tr>
                <td><code style="color: var(--primary); font-weight:600">${escapeHtml(item.transactionId)}</code></td>
                <td style="color: var(--text-dim); font-size:12px;">${formatDateTime(item.createdAt)}</td>
                <td>${badge}</td>
                <td>
                    <div class="part-cell">
                        <span class="part-name">${escapeHtml(item.partName || item.partId)}</span>
                        <span class="part-code">${escapeHtml(item.partId)}</span>
                    </div>
                </td>
                <td>
                    <strong style="color: ${isImport ? 'var(--success)' : 'var(--purple)'}">
                        ${isImport ? '+' : '-'}${item.quantity}
                    </strong>
                </td>
                <td>
                    ${item.ticketId ? `<span class="badge badge-cyan">${escapeHtml(item.ticketId)}</span>` : '<span style="color: var(--text-dim)">—</span>'}
                </td>
                <td>${escapeHtml(item.performedBy || '—')}</td>
                <td style="color: var(--text-muted); font-size:12px;">${escapeHtml(item.note || '—')}</td>
            </tr>
        `;
    });

    html += `</tbody></table>`;
    container.innerHTML = html;
}

// =============================================================================
// MODALS & POPUPS
// =============================================================================
function setupModals() {
    // 1. Modal Thêm linh kiện mới (Tiêu chí S1 - CRUD Create Part)
    const formCreatePart = document.getElementById('form-create-part');
    if (formCreatePart) {
        formCreatePart.addEventListener('submit', async (e) => {
            e.preventDefault();
            const partCode = document.getElementById('create-part-code').value.trim().toUpperCase();
            const partName = document.getElementById('create-part-name').value.trim();
            const category = document.getElementById('create-part-category').value;
            const unit = document.getElementById('create-part-unit').value.trim();
            const unitPrice = document.getElementById('create-part-price').value;
            const initialQuantity = Number(document.getElementById('create-part-init-qty').value || 0);
            const minThreshold = Number(document.getElementById('create-part-min-thresh').value || 5);
            const submitBtn = document.getElementById('btn-submit-create-part');

            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Đang lưu...';
            }

            try {
                // Tạo linh kiện trong danh mục chuẩn (POST /api/v1/parts)
                await window.apiClient.createPart({ partCode, partName, category, unit, unitPrice });

                // Khởi tạo bản ghi tồn kho cho trung tâm hiện tại (POST /api/v1/stock)
                try {
                    await window.apiClient.createStock({ partCode, initialQuantity, minThreshold });
                } catch (stockErr) {
                    console.warn('Khởi tạo tồn kho báo:', stockErr.message);
                }

                showToast(`✓ Đã thêm mới linh kiện "${partName}" (${partCode}) vào hệ thống!`, 'success');
                closeModal('modal-create-part');
                loadStockData();
            } catch (err) {
                showToast(`Lỗi thêm linh kiện: ${err.message}`, 'error');
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = '✓ Lưu linh kiện';
                }
            }
        });
    }

    // 2. Modal Chỉnh sửa linh kiện (Tiêu chí S1 - CRUD Update Part)
    const formEditPart = document.getElementById('form-edit-part');
    if (formEditPart) {
        formEditPart.addEventListener('submit', async (e) => {
            e.preventDefault();
            const partCode = document.getElementById('edit-part-code').value;
            const partName = document.getElementById('edit-part-name').value.trim();
            const category = document.getElementById('edit-part-category').value;
            const unit = document.getElementById('edit-part-unit').value.trim();
            const unitPrice = document.getElementById('edit-part-price').value;
            const submitBtn = document.getElementById('btn-submit-edit-part');

            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Đang cập nhật...';
            }

            try {
                await window.apiClient.updatePart(partCode, { partName, category, unit, unitPrice, isActive: true });
                showToast(`✓ Cập nhật thông tin linh kiện "${partName}" (${partCode}) thành công!`, 'success');
                closeModal('modal-edit-part');
                loadStockData();
            } catch (err) {
                showToast(`Lỗi cập nhật linh kiện: ${err.message}`, 'error');
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = '✓ Cập nhật';
                }
            }
        });
    }

    // 3. Modal Điều chỉnh ngưỡng cảnh báo tồn (Update Stock)
    const formThreshold = document.getElementById('form-threshold');
    if (formThreshold) {
        formThreshold.addEventListener('submit', async (e) => {
            e.preventDefault();
            const partId = document.getElementById('modal-threshold-part-id').value;
            const threshold = document.getElementById('modal-threshold-val').value;

            try {
                await window.apiClient.updateThreshold(partId, threshold);
                showToast(`Đã cập nhật ngưỡng tồn tối thiểu cho ${partId} thành ${threshold}`, 'success');
                closeModal('modal-threshold');
                loadStockData();
            } catch (err) {
                showToast(`Lỗi: ${err.message}`, 'error');
            }
        });
    }

    // 4. Modal Nhập kho nhanh (Create Transaction)
    const formImport = document.getElementById('form-import-quick');
    if (formImport) {
        formImport.addEventListener('submit', async (e) => {
            e.preventDefault();
            const partId = document.getElementById('modal-import-part-id').value;
            const quantity = document.getElementById('modal-import-quantity').value;
            const performedBy = document.getElementById('modal-import-performed-by').value;
            const note = document.getElementById('modal-import-note').value;

            try {
                const res = await window.apiClient.importPart({ partId, quantity, performedBy, note });
                showToast(`✓ Nhập kho thành công ${quantity} cái cho ${partId}! Số tồn mới: ${res.data.remainingStock}`, 'success');
                closeModal('modal-import');
                loadStockData();
            } catch (err) {
                showToast(`Lỗi nhập kho: ${err.message}`, 'error');
            }
        });
    }
}

window.openCreatePartModal = function() {
    const user = window.apiClient.getCurrentUser();
    if (!user || user.role !== 'MANAGER') {
        showToast('⚠️ Quyền bị từ chối (403): Chỉ Quản lý kho mới được phép thêm mới linh kiện!', 'error');
        return;
    }
    const form = document.getElementById('form-create-part');
    if (form) form.reset();
    document.getElementById('create-part-unit').value = 'Cái';
    document.getElementById('create-part-init-qty').value = '10';
    document.getElementById('create-part-min-thresh').value = '5';
    openModal('modal-create-part');
};

window.openEditPartModal = function(partCode, partName, category, unit, unitPrice) {
    const user = window.apiClient.getCurrentUser();
    if (!user || user.role !== 'MANAGER') {
        showToast('⚠️ Quyền bị từ chối (403): Chỉ Quản lý kho mới được phép chỉnh sửa linh kiện!', 'error');
        return;
    }
    document.getElementById('edit-part-code').value = partCode;
    document.getElementById('edit-part-code-display').textContent = partCode;
    document.getElementById('edit-part-name').value = partName;
    document.getElementById('edit-part-category').value = category || 'MAN_HINH';
    document.getElementById('edit-part-unit').value = unit || 'Cái';
    document.getElementById('edit-part-price').value = unitPrice || 0;
    openModal('modal-edit-part');
};

window.deletePartAction = async function(partCode, partName) {
    const user = window.apiClient.getCurrentUser();
    if (!user || user.role !== 'MANAGER') {
        showToast('⚠️ Quyền bị từ chối (403): Chỉ Quản lý kho mới được phép xóa linh kiện!', 'error');
        return;
    }

    const confirmed = confirm(`Bạn có chắc chắn muốn xóa linh kiện "${partName}" (${partCode}) khỏi hệ thống không?\n\nLưu ý: Hệ thống sẽ thực hiện soft-delete để bảo toàn lịch sử giao dịch.`);
    if (!confirmed) return;

    try {
        await window.apiClient.deletePart(partCode);
        showToast(`✓ Đã xóa linh kiện "${partName}" (${partCode}) thành công!`, 'success');
        loadStockData();
    } catch (err) {
        showToast(`Lỗi xóa linh kiện: ${err.message}`, 'error');
    }
};

window.openThresholdModal = function(partId, currentThreshold, partName) {
    const user = window.apiClient.getCurrentUser();
    if (!user || user.role !== 'MANAGER') {
        showToast('⚠️ Quyền bị từ chối (403): Chỉ Quản lý kho mới được phép điều chỉnh định mức tồn!', 'error');
        return;
    }
    document.getElementById('modal-threshold-part-id').value = partId;
    document.getElementById('modal-threshold-part-name').textContent = `${partId} — ${partName}`;
    document.getElementById('modal-threshold-val').value = currentThreshold;
    openModal('modal-threshold');
};

window.openImportModal = function() {
    const user = window.apiClient.getCurrentUser();
    if (!user || user.role !== 'MANAGER') {
        showToast('⚠️ Quyền bị từ chối (403): Chỉ Quản lý kho mới được phép Nhập kho linh kiện!', 'error');
        return;
    }
    openModal('modal-import');
};

function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('active');
}

window.closeModal = function(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('active');
};

// =============================================================================
// TRÌNH BÀY 3 TRẠNG THÁI: LOADING, EMPTY, ERROR (TIÊU CHUẨN S4)
// =============================================================================
function renderLoadingState(container, message = 'Đang tải dữ liệu...') {
    container.innerHTML = `
        <div class="state-container">
            <div class="spinner"></div>
            <div class="state-title">${escapeHtml(message)}</div>
            <div class="state-desc">Đang truy vấn dữ liệu từ API CSDL MySQL...</div>
        </div>
    `;
}

function renderEmptyState(container, title, desc, onAction = null, actionText = 'Làm mới') {
    let actionBtnHtml = '';
    if (onAction && actionText) {
        actionBtnHtml = `<button class="btn btn-secondary btn-sm" id="btn-empty-action" style="margin-top: 10px;">${escapeHtml(actionText)}</button>`;
    }

    container.innerHTML = `
        <div class="state-container">
            <div class="state-icon">📂</div>
            <div class="state-title">${escapeHtml(title)}</div>
            <div class="state-desc">${escapeHtml(desc)}</div>
            ${actionBtnHtml}
        </div>
    `;

    if (onAction) {
        const btn = document.getElementById('btn-empty-action');
        if (btn) btn.addEventListener('click', onAction);
    }
}

function renderErrorState(container, errorMessage, onRetry) {
    container.innerHTML = `
        <div class="state-container">
            <div class="state-icon" style="color: var(--danger)">⚠️</div>
            <div class="state-title" style="color: var(--danger)">Lỗi tải dữ liệu</div>
            <div class="state-desc">${escapeHtml(errorMessage)}</div>
            <button class="btn btn-primary btn-sm" id="btn-retry-action" style="margin-top: 14px;">
                🔄 Thử lại ngay
            </button>
        </div>
    `;

    const btn = document.getElementById('btn-retry-action');
    if (btn && onRetry) {
        btn.addEventListener('click', onRetry);
    }
}

// =============================================================================
// TIỆN ÍCH CHUNG
// =============================================================================
function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${escapeHtml(message)}</span>
        <button style="background:none;border:none;color:#fff;cursor:pointer;margin-left:12px;">✕</button>
    `;

    toast.querySelector('button').addEventListener('click', () => toast.remove());
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 300);
    }, 4500);
}

function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function formatDateTime(dtStr) {
    if (!dtStr) return '—';
    try {
        const d = new Date(dtStr);
        if (isNaN(d.getTime())) return dtStr;
        return d.toLocaleString('vi-VN', {
            hour: '2-digit', minute: '2-digit',
            day: '2-digit', month: '2-digit', year: 'numeric'
        });
    } catch {
        return dtStr;
    }
}
