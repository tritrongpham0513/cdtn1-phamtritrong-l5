/**
 * =============================================================================
 * LỚP CLIENT GỌI API RIÊNG BIỆT (API CLIENT LAYER)
 * Đáp ứng tiêu chuẩn Thành phần 3 (Mục 2.3 - Bài tập 2):
 * "Mọi lệnh gọi API đi qua một lớp client riêng"
 * =============================================================================
 */

class InventoryApiClient {
    constructor(baseUrl = '') {
        this.baseUrl = baseUrl;
        this.token = localStorage.getItem('auth_token') || null;
        try {
            this.currentUser = JSON.parse(localStorage.getItem('auth_user') || 'null');
        } catch {
            this.currentUser = null;
        }
        this.currentCenterId = this.currentUser?.centerId ? Number(this.currentUser.centerId) : 1;
    }

    setCenterId(centerId) {
        this.currentCenterId = Number(centerId);
    }

    getCenterId() {
        return this.currentCenterId;
    }

    isAuthenticated() {
        return !!this.token && !!this.currentUser;
    }

    getCurrentUser() {
        return this.currentUser;
    }

    getToken() {
        return this.token;
    }

    /**
     * Xác thực đăng nhập cơ bản (Thành phần 3 - Tiêu chí Track SE)
     */
    async login(username, password) {
        const response = await this.request('/api/v1/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });

        if (response && response.success && response.data) {
            const user = response.data;
            this.token = user.token;
            this.currentUser = user;
            if (user.centerId) {
                this.currentCenterId = Number(user.centerId);
            }
            localStorage.setItem('auth_token', this.token);
            localStorage.setItem('auth_user', JSON.stringify(this.currentUser));
            return user;
        }
        throw new Error('Đăng nhập không thành công');
    }

    /**
     * Đăng xuất khỏi hệ thống
     */
    logout() {
        this.token = null;
        this.currentUser = null;
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_user');
    }

    /**
     * Hàm gọi fetch dùng chung có gắn header và bắt lỗi mạng
     */
    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'X-Center-Id': this.currentCenterId.toString()
        };

        if (this.token) {
            defaultHeaders['Authorization'] = `Bearer ${this.token}`;
        }

        const config = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...(options.headers || {})
            }
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const errorMessage = data?.error?.message 
                    || data?.message 
                    || `Yêu cầu thất bại với mã lỗi HTTP ${response.status}`;
                const errorCode = data?.error?.code || 'API_ERROR';
                const errorDetails = data?.error?.details || [];

                const error = new Error(errorMessage);
                error.status = response.status;
                error.code = errorCode;
                error.details = errorDetails;
                throw error;
            }

            return data;
        } catch (err) {
            if (err.name === 'TypeError' && err.message.includes('fetch')) {
                const networkErr = new Error('Không thể kết nối đến máy chủ API (http://localhost:8080). Vui lòng kiểm tra lại dịch vụ backend.');
                networkErr.isNetworkError = true;
                networkErr.code = 'NETWORK_ERROR';
                throw networkErr;
            }
            throw err;
        }
    }

    // =========================================================================
    // 1. CÁC API TỒN KHO (STOCK)
    // =========================================================================

    /**
     * Tra cứu danh sách tồn kho có lọc và phân trang (Màn hình M1)
     */
    async getStockList({ keyword = '', isLowStock = false, page = 1, limit = 20 } = {}) {
        const params = new URLSearchParams({
            centerId: this.currentCenterId,
            page: page.toString(),
            limit: limit.toString()
        });
        if (keyword.trim()) params.append('keyword', keyword.trim());
        if (isLowStock) params.append('isLowStock', 'true');

        return this.request(`/api/v1/stock?${params.toString()}`, { method: 'GET' });
    }

    /**
     * Xem chi tiết tồn kho 1 linh kiện
     */
    async getStockDetail(partCode) {
        return this.request(`/api/v1/stock/${encodeURIComponent(partCode)}?centerId=${this.currentCenterId}`, {
            method: 'GET'
        });
    }

    /**
     * Điều chỉnh ngưỡng cảnh báo tồn tối thiểu
     */
    async updateThreshold(partId, minThreshold) {
        return this.request('/api/v1/stock/threshold', {
            method: 'PUT',
            body: JSON.stringify({
                partId: partId,
                minThreshold: Number(minThreshold)
            })
        });
    }

    /**
     * Khởi tạo bản ghi tồn kho cho linh kiện tại trung tâm (Create Stock)
     */
    async createStock({ partCode, initialQuantity = 0, minThreshold = 5 }) {
        return this.request('/api/v1/stock', {
            method: 'POST',
            body: JSON.stringify({
                partCode: partCode.trim().toUpperCase(),
                initialQuantity: Number(initialQuantity),
                minThreshold: Number(minThreshold)
            })
        });
    }

    /**
     * Xóa bản ghi tồn kho khi số lượng bằng 0 (Delete Stock)
     */
    async deleteStock(partCode) {
        return this.request(`/api/v1/stock/${encodeURIComponent(partCode)}`, {
            method: 'DELETE'
        });
    }

    // =========================================================================
    // 2. CÁC API DANH MỤC LINH KIỆN (PARTS - TIÊU CHÍ S1 CRUD)
    // =========================================================================

    /**
     * Thêm mới linh kiện vào danh mục chuẩn hóa (Create Part)
     */
    async createPart({ partCode, partName, category, unit = 'Cái', unitPrice }) {
        return this.request('/api/v1/parts', {
            method: 'POST',
            body: JSON.stringify({
                partCode: partCode.trim().toUpperCase(),
                partName: partName.trim(),
                category: category.trim(),
                unit: unit.trim(),
                unitPrice: Number(unitPrice)
            })
        });
    }

    /**
     * Tra cứu chi tiết một linh kiện theo mã (Read single)
     */
    async getPartByCode(partCode) {
        return this.request(`/api/v1/parts/${encodeURIComponent(partCode)}`, {
            method: 'GET'
        });
    }

    /**
     * Cập nhật thông tin linh kiện (Update Part)
     */
    async updatePart(partCode, { partName, category, unit = 'Cái', unitPrice, isActive = true }) {
        return this.request(`/api/v1/parts/${encodeURIComponent(partCode)}`, {
            method: 'PUT',
            body: JSON.stringify({
                partName: partName.trim(),
                category: category.trim(),
                unit: unit.trim(),
                unitPrice: Number(unitPrice),
                isActive: isActive
            })
        });
    }

    /**
     * Xóa linh kiện khỏi danh mục (Delete Part - Soft delete)
     */
    async deletePart(partCode) {
        return this.request(`/api/v1/parts/${encodeURIComponent(partCode)}`, {
            method: 'DELETE'
        });
    }

    // =========================================================================
    // 3. CÁC API GIAO DỊCH (TRANSACTIONS)
    // =========================================================================

    /**
     * Xuất kho linh kiện cho phiếu bảo hành (Màn hình M2)
     */
    async exportPart({ partId, ticketId, quantity, performedBy, note = '' }) {
        return this.request('/api/v1/transactions/export', {
            method: 'POST',
            body: JSON.stringify({
                partId: partId.trim(),
                ticketId: ticketId.trim(),
                quantity: Number(quantity),
                performedBy: performedBy.trim(),
                note: note.trim()
            })
        });
    }

    /**
     * Nhập kho bổ sung linh kiện
     */
    async importPart({ partId, quantity, performedBy, note = '' }) {
        return this.request('/api/v1/transactions/import', {
            method: 'POST',
            body: JSON.stringify({
                partId: partId.trim(),
                quantity: Number(quantity),
                performedBy: performedBy.trim(),
                note: note.trim()
            })
        });
    }

    /**
     * Tra cứu lịch sử giao dịch kho (Màn hình M3)
     */
    async getTransactions({ partId = '', type = '', fromDate = '', toDate = '', page = 1, limit = 20 } = {}) {
        const params = new URLSearchParams({
            page: page.toString(),
            limit: limit.toString()
        });
        if (partId.trim()) params.append('partId', partId.trim());
        if (type.trim()) params.append('type', type.trim());
        if (fromDate) params.append('fromDate', fromDate);
        if (toDate) params.append('toDate', toDate);

        return this.request(`/api/v1/transactions?${params.toString()}`, { method: 'GET' });
    }

    /**
     * Xem danh sách linh kiện đã dùng theo phiếu bảo hành
     */
    async getPartsByTicket(ticketCode) {
        return this.request(`/api/v1/tickets/${encodeURIComponent(ticketCode)}/parts`, {
            method: 'GET'
        });
    }

    /**
     * Kiểm tra sức khỏe hệ thống
     */
    async checkHealth() {
        return this.request('/api/health', { method: 'GET' });
    }
}

// Khởi tạo instance dùng chung cho toàn bộ giao diện
window.apiClient = new InventoryApiClient();
