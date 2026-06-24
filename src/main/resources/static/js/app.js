(function () {
    'use strict';
  
    // ========================================================
    //  GLOBAL HELPERS
    // ========================================================
    var token = localStorage.getItem('token');
    var role = localStorage.getItem('role');
    var userId = localStorage.getItem('userId');
  
    function getHeaders() {
      return {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : ''
      };
    }
  
    function redirect(url) {
      window.location.replace(url);
    }
  
    function esc(str) {
      if (str === null || str === undefined) return '';
      return String(str).replace(/[&<>"']/g, function (m) {
        return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[m];
      });
    }
  
    function formatVol(val) {
      if (val === null || val === undefined) return '0.00 MT';
      return parseFloat(val).toFixed(2) + ' MT';
    }

    function formatMoney(val) {
        if (val === null || val === undefined) return '₹ 0.00';
        return '₹ ' + parseFloat(val).toFixed(2);
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        try { 
            return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }); 
        } catch (e) { return dateStr; }
    }
  
    window.showToast = function(message, type) {
        var container = document.getElementById('toast-container');
        if(!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }
        var toast = document.createElement('div');
        toast.className = 'toast ' + (type || 'info');
        toast.textContent = message;
        container.appendChild(toast);
        setTimeout(function(){ toast.remove(); }, 3000);
    };
    function showAlert(id, msg, type) {
      var el = document.getElementById(id);
      var span = document.getElementById(id.replace('-alert', '-msg'));
      if (!el || !span) return;
      el.className = 'alert ' + (type === 'error' ? 'alert-error' : 'alert-success');
      el.style.display = 'block';
      span.textContent = msg;
      setTimeout(function () { el.style.display = 'none'; }, 4000);
    }
  
    function initTabs() {
      var tabBtns = document.querySelectorAll('.tab-btn');
      tabBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
          var targetTab = btn.getAttribute('data-tab');
          var parent = btn.closest('.card');
          if(!parent) return;
          parent.querySelectorAll('.tab-btn').forEach(function (b) { b.classList.remove('active'); });
          parent.querySelectorAll('.tab-content').forEach(function (tc) { tc.classList.remove('active'); });
          btn.classList.add('active');
          var panel = document.getElementById(targetTab);
          if (panel) panel.classList.add('active');
        });
      });
    }

    function handleApiError(res) {
        if (res.status === 401 || res.status === 403) {
            localStorage.clear();
            redirect('/2b3c4d');
            throw new Error("Unauthorized");
        }
        return res.json().then(function(data) { throw new Error(data.message || 'Server Error'); });
    }
  
    // ========================================================
    //  INIT LOGIC
    // ========================================================
    function initSidebar() {
      var links = document.querySelectorAll('.sidebar-link');
      if (links.length === 0) return;
      
      links.forEach(function(link) {
          link.addEventListener('click', function(e) {
              e.preventDefault();
              
              // Remove active from all links
              links.forEach(function(l) { l.classList.remove('active'); });
              this.classList.add('active');
              
              // Hide all sections
              document.querySelectorAll('.view-section').forEach(function(sec) {
                  sec.classList.remove('active');
              });
              
              // Show target section
              var targetId = this.getAttribute('data-target');
              var targetSec = document.getElementById(targetId);
              if (targetSec) targetSec.classList.add('active');
          });
      });
    }

    function initCarousel() {
        var track = document.getElementById('carousel-track');
        var slides = Array.from(document.querySelectorAll('.carousel-slide'));
        var nextBtn = document.getElementById('carousel-next');
        var prevBtn = document.getElementById('carousel-prev');
        var dots = Array.from(document.querySelectorAll('.dot'));
        if(!track || slides.length === 0) return;

        var currentSlide = 0;
        var slideInterval;

        function updateCarousel(index) {
            track.style.transform = 'translateX(-' + (index * 100) + '%)';
            slides.forEach(function(s) { s.classList.remove('active'); });
            dots.forEach(function(d) { d.classList.remove('active'); });
            
            slides[index].classList.add('active');
            if(dots[index]) dots[index].classList.add('active');
            currentSlide = index;
        }

        if(nextBtn) {
            nextBtn.addEventListener('click', function() {
                var nextIndex = (currentSlide + 1) % slides.length;
                updateCarousel(nextIndex);
                resetInterval();
            });
        }

        if(prevBtn) {
            prevBtn.addEventListener('click', function() {
                var prevIndex = (currentSlide - 1 + slides.length) % slides.length;
                updateCarousel(prevIndex);
                resetInterval();
            });
        }

        dots.forEach(function(dot, index) {
            dot.addEventListener('click', function() {
                updateCarousel(index);
                resetInterval();
            });
        });

        function startInterval() {
            slideInterval = setInterval(function() {
                var nextIndex = (currentSlide + 1) % slides.length;
                updateCarousel(nextIndex);
            }, 5000);
        }

        function resetInterval() {
            clearInterval(slideInterval);
            startInterval();
        }

        startInterval();
    }

    document.addEventListener('DOMContentLoaded', function () {
      
      initTabs();
      initSidebar();
      initCarousel();

      var path = window.location.pathname;
      if (path.endsWith('d4e5f6')) {
        if (!token || role !== 'ADMIN') { redirect('/2b3c4d'); return; }
        document.getElementById('admin-name').textContent = localStorage.getItem('username') || 'Admin';
        initAdmin();
      } else if (path.endsWith('7a8b9c')) {
        if (!token || role !== 'DISTRIBUTOR') { redirect('/2b3c4d'); return; }
        document.getElementById('distributor-name').textContent = localStorage.getItem('username') || 'Distributor';
        initDistributor();
      }
  
      var logoutBtn = document.getElementById('logout-btn');
      if (logoutBtn) {
        logoutBtn.addEventListener('click', function (e) {
          e.preventDefault();
          localStorage.clear();
          redirect('/2b3c4d');
        });
      }

      var loginForm = document.getElementById('login-form');
      if (loginForm) {
        initLoginListener();
      }
    });
  
    // ========================================================
    //  LOGIN PAGE
    // ========================================================
    window.handleLoginClick = function() {
        var form = document.getElementById('login-form');
        if (form) {
            var event = new Event('submit', { cancelable: true });
            form.dispatchEvent(event);
        }
    };

    function initLoginListener() {
        var form = document.getElementById('login-form');
        var submitBtn = form.querySelector('button[type="submit"]') || form.querySelector('button[onclick="handleLoginClick()"]');

        form.addEventListener('submit', function (e) {
            e.preventDefault();

            var username = document.getElementById('username').value.trim();
            var password = document.getElementById('password').value;
            var roleSelect = document.getElementById('role') ? document.getElementById('role').value : '';

            if (!username || !password) {
                showLoginError('Please enter both username and password.');
                return;
            }

            var originalBtnText = submitBtn ? submitBtn.textContent : 'Login';
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Authenticating...';
            }

            var errorDiv = document.getElementById('login-error');
            if (errorDiv) errorDiv.style.display = 'none';
            var successDiv = document.getElementById('login-success');
            if (successDiv) successDiv.style.display = 'none';

            fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: username, password: password })
            })
            .then(function (res) {
                if (!res.ok) {
                    return res.json().then(function (body) {
                        throw new Error(body.message || 'Invalid credentials');
                    }).catch(function (parseErr) {
                        if (parseErr.message && parseErr.message !== 'Invalid credentials') throw parseErr;
                        throw new Error('Invalid username or password.');
                    });
                }
                return res.json();
            })
            .then(function (data) {
                if (!data.success) {
                    showLoginError(data.message || 'Login failed.');
                    if (submitBtn) {
                        submitBtn.disabled = false;
                        submitBtn.textContent = originalBtnText;
                    }
                    return;
                }

                var user = data.user;
                var userRole = (user.role || '').toUpperCase();

                if (roleSelect) {
                    var roleMap = { 'SAIL Admin': 'ADMIN', 'Distributor': 'DISTRIBUTOR' };
                    var expectedRole = roleMap[roleSelect];
                    if (userRole !== expectedRole) {
                        showLoginError('Selected role does not match your account. Please choose the correct role.');
                        if (submitBtn) {
                            submitBtn.disabled = false;
                            submitBtn.textContent = originalBtnText;
                        }
                        return;
                    }
                }

                localStorage.setItem('token', 'mock-token');
                localStorage.setItem('role', userRole);
                localStorage.setItem('userId', user.id);
                localStorage.setItem('username', user.username);

                if (submitBtn) {
                    submitBtn.textContent = 'Success! Redirecting...';
                }

                showLoginSuccess('Login successful! Redirecting to dashboard...');

                setTimeout(function () {
                    if (userRole === 'ADMIN') {
                        redirect('/d4e5f6');
                    } else {
                        redirect('/7a8b9c');
                    }
                }, 2500);
            })
            .catch(function (err) {
                showLoginError(err.message || 'Login failed. Please try again.');
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = originalBtnText;
                }
            });
        });
    }

    function showLoginError(msg) {
        var errorDiv = document.getElementById('login-error');
        var errorMsg = document.getElementById('login-error-msg');
        if (errorDiv && errorMsg) {
            errorMsg.textContent = msg;
            errorDiv.style.display = 'block';
            setTimeout(function () { errorDiv.style.display = 'none'; }, 5000);
        }
    }

    function showLoginSuccess(msg) {
        var successDiv = document.getElementById('login-success');
        var successMsg = document.getElementById('login-success-msg');
        if (successDiv && successMsg) {
            successMsg.textContent = msg;
            successDiv.style.display = 'block';
        }
    }
  
    // ========================================================
    //  ADMIN PAGE
    // ========================================================
    function initAdmin() {
      loadAdminDashboard();
      loadDistributorsGrid();
      loadUnitsDropdown();
      loadPendingOrders();
      loadAllOrders(); // Add Order History load
      loadInventory();
      loadPlantInventory();
      
      var ledgerSelect = document.getElementById('ledger-distributor-select');
      if (ledgerSelect) {
          ledgerSelect.addEventListener('change', function(e) {
              if (e.target.value) {
                  window.viewLedger(e.target.value);
              } else {
                  var analysisView = document.getElementById('analysis-view');
                  if(analysisView) analysisView.classList.add('hide');
              }
          });
      }
  
      // Add Distributor
      document.getElementById('add-distributor-form').addEventListener('submit', function(e) {
          e.preventDefault();
          var payload = {
              username: document.getElementById('dist-username').value,
              password: document.getElementById('dist-password').value,
              name: document.getElementById('dist-fullname').value,
              unitId: document.getElementById('dist-unit').value,
              contactEmail: document.getElementById('dist-email').value,
              contactPhone: document.getElementById('dist-phone').value,
              region: document.getElementById('dist-region').value
          };
          fetch('/api/admin/distributors', {
              method: 'POST',
              headers: getHeaders(),
              body: JSON.stringify(payload)
          }).then(function(res){
              if(!res.ok) return handleApiError(res);
              return res.json();
          }).then(function(){
              showAlert('add-dist-alert', 'Distributor created successfully', 'success');
              document.getElementById('add-distributor-form').reset();
              loadAdminDashboard();
              loadDistributorsGrid();
          }).catch(function(err){
              showAlert('add-dist-alert', err.message, 'error');
          });
      });

      // Assign Target
      document.getElementById('assign-target-form').addEventListener('submit', function(e) {
          e.preventDefault();
          var payload = {
              distributorId: document.getElementById('target-distributor').value,
              targetVolume: document.getElementById('target-amount').value,
              fiscalYear: document.getElementById('target-fy').value,
              quarter: document.getElementById('target-quarter').value
          };
          fetch('/api/admin/targets', {
              method: 'POST',
              headers: getHeaders(),
              body: JSON.stringify(payload)
          }).then(function(res){
              if(!res.ok) return handleApiError(res);
              return res.json();
          }).then(function(){
              showAlert('assign-target-alert', 'Target assigned successfully', 'success');
              document.getElementById('assign-target-form').reset();
              loadDistributorsGrid();
          }).catch(function(err){
              showAlert('assign-target-alert', err.message, 'error');
          });
      });

      // Inventory Form
      document.getElementById('inventory-form').addEventListener('submit', function(e) {
          e.preventDefault();
          var payload = {
              distributorId: document.getElementById('inv-distributor').value,
              productCategory: document.getElementById('inv-product').value,
              quantity: document.getElementById('inv-quantity').value,
              threshold: document.getElementById('inv-threshold').value,
              pricePerMt: document.getElementById('inv-price').value
          };
          fetch('/api/admin/inventory', {
              method: 'POST',
              headers: getHeaders(),
              body: JSON.stringify(payload)
          }).then(function(res){
              if(!res.ok) return handleApiError(res);
              return res.json();
          }).then(function(){
              closeInventoryModal();
              loadInventory();
          }).catch(function(err){
              window.showToast('Error updating inventory: ' + err.message, 'error');
          });
      });


    }

    // --- Admin API Calls ---
    function loadAdminDashboard() {
        fetch('/api/admin/dashboard', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(data){
            document.getElementById('total-distributors').textContent = data.totalDistributors;
            document.getElementById('total-revenue').textContent = formatVol(data.totalSales);
            document.getElementById('pending-orders-count').textContent = data.pendingOrdersCount;
            document.getElementById('low-stock-count').textContent = data.lowStockAlertsCount;
        });
    }

    function loadUnitsDropdown() {
        fetch('/api/admin/units', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(units){
            var sel = document.getElementById('dist-unit');
            if(sel) {
                var html = '<option value="">-- Select --</option>';
                units.forEach(function(u){ html += '<option value="'+u.id+'">'+esc(u.shortCode)+' - '+esc(u.name)+'</option>'; });
                sel.innerHTML = html;
            }
        });
    }

    function loadDistributorsGrid() {
        fetch('/api/admin/distributors', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(distributors){
            var grid = document.getElementById('distributors-grid');
            var select = document.getElementById('target-distributor');
            var invSelect = document.getElementById('inv-distributor');
            var ledgerSelect = document.getElementById('ledger-distributor-select');

            if(select) select.innerHTML = '<option value="">-- Select --</option>';
            if(invSelect) invSelect.innerHTML = '<option value="">-- Select --</option>';
            if(ledgerSelect) ledgerSelect.innerHTML = '<option value="">-- Choose Distributor --</option>';

            if(!distributors || distributors.length === 0) {
                if(grid) grid.innerHTML = '<div class="text-center text-muted" style="grid-column:1/-1;">No distributors found.</div>';
                return;
            }

            var html = '';
            var optHtml = '';
            distributors.forEach(function(d){
                optHtml += '<option value="'+d.id+'">'+esc(d.name)+' ('+esc(d.unit ? d.unit.shortCode : '')+')</option>';
                
                var pct = d.performanceScore || 0;
                var badgeClass = pct >= 80 ? 'badge-success' : (pct >= 50 ? 'badge-warning' : 'badge-danger');
                var unitCode = d.unit ? d.unit.shortCode : 'SAIL';

                html += '<div class="distributor-card">' +
                        '  <div class="d-header">' +
                        '    <div class="d-title">'+esc(d.name)+'</div>' +
                        '    <div class="d-unit">'+esc(unitCode)+'</div>' +
                        '  </div>' +
                        '  <div class="d-stats">' +
                        '    <div class="d-stat"><span class="label">Sales</span><span class="value">'+formatVol(d.totalSales)+'</span></div>' +
                        '    <div class="d-stat"><span class="label">Target</span><span class="value">'+formatVol(d.totalTarget)+'</span></div>' +
                        '    <div class="d-stat"><span class="label">Perf.</span><span class="badge '+badgeClass+'">'+pct.toFixed(1)+'%</span></div>' +
                        '  </div>' +
                        '</div>';
            });
            if(grid) grid.innerHTML = html;
            if(select) select.innerHTML += optHtml;
            if(invSelect) invSelect.innerHTML += optHtml;
            if(ledgerSelect) ledgerSelect.innerHTML += optHtml;
        });
    }

    function loadPendingOrders() {
        fetch('/api/admin/orders/pending', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(orders){
            var tbody = document.getElementById('pending-orders-tbody');
            if(!tbody) return;
            if(!orders || orders.length===0){
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No pending orders.</td></tr>';
                return;
            }
            var html = '';
            orders.forEach(function(o){
                html += '<tr>' +
                        '<td>#'+o.id+'</td>' +
                        '<td>'+esc(o.distributorName)+'</td>' +
                        '<td><strong>'+esc(o.productCategory)+'</strong></td>' +
                        '<td>'+formatVol(o.quantity)+'</td>' +
                        '<td>'+formatMoney(o.totalPrice)+'</td>' +
                        '<td>' +
                        '  <button class="btn btn-sm btn-success" onclick="window.approveOrder('+o.id+')">Approve</button> ' +
                        '  <button class="btn btn-sm btn-danger" onclick="window.rejectOrder('+o.id+')">Reject</button>' +
                        '</td>' +
                        '</tr>';
            });
            tbody.innerHTML = html;
        });
    }

    function loadAllOrders() {
        fetch('/api/admin/orders', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(orders){
            var tbody = document.getElementById('order-history-tbody');
            if(!tbody) return;
            if(!orders || orders.length===0){
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No orders found.</td></tr>';
                return;
            }
            var html = '';
            orders.forEach(function(o){
                var statusBadge = '';
                if(o.status === 'PENDING') statusBadge = '<span class="badge badge-warning">Pending</span>';
                else if(o.status === 'APPROVED') statusBadge = '<span class="badge badge-success">Approved</span>';
                else if(o.status === 'REJECTED') statusBadge = '<span class="badge badge-danger">Rejected</span>';
                else if(o.status === 'DELIVERED') statusBadge = '<span class="badge badge-success">Delivered</span>';
                else statusBadge = '<span class="badge badge-outline">'+esc(o.status)+'</span>';

                html += '<tr>' +
                        '<td>#'+o.id+'</td>' +
                        '<td>'+esc(o.distributorName)+'</td>' +
                        '<td><strong>'+esc(o.productCategory)+'</strong></td>' +
                        '<td>'+formatVol(o.quantity)+'</td>' +
                        '<td>'+formatMoney(o.totalPrice)+'</td>' +
                        '<td>'+statusBadge+'</td>' +
                        '</tr>';
            });
            tbody.innerHTML = html;
        });
    }

    function loadInventory() {
        fetch('/api/admin/inventory/low-stock', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(items){
            var tbody = document.getElementById('inventory-tbody');
            if(!tbody) return;
            if(!items || items.length===0){
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No distributor inventory data.</td></tr>';
                return;
            }
            var html = '';
            items.forEach(function(i){
                var status = i.isLowStock ? '<span class="badge badge-danger">LOW STOCK</span>' : '<span class="badge badge-success">OK</span>';
                html += '<tr>' +
                        '<td>'+esc(i.distributorName)+'</td>' +
                        '<td><strong>'+esc(i.productCategory)+'</strong></td>' +
                        '<td>'+formatVol(i.quantity)+'</td>' +
                        '<td>'+formatVol(i.threshold)+'</td>' +
                        '<td>'+status+'</td>' +
                        '</tr>';
            });
            tbody.innerHTML = html;
        });
    }

    window.plantDataMap = {};
    
    function loadPlantInventory() {
        fetch('/api/admin/plant-inventory', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(items){
            var container = document.getElementById('plant-boxes-container');
            if(!container) return;
            
            if(!items || items.length===0){
                container.innerHTML = '<div class="text-center text-muted" style="grid-column: 1 / -1;">No plant inventory data.</div>';
                return;
            }
            
            // Group by unit
            var plants = {};
            items.forEach(function(i){
                var code = i.unitCode || 'SAIL';
                if(!plants[code]) {
                    plants[code] = { 
                        name: i.unitName || code, 
                        code: code, 
                        capacity: i.dailyCapacity || 'Unknown', 
                        hasCritical: false, 
                        items: [] 
                    };
                }
                plants[code].items.push(i);
                if(i.isLowStock) plants[code].hasCritical = true;
            });
            
            window.plantDataMap = plants;
            
            var html = '';
            Object.values(plants).forEach(function(p){
                var statusBadge = p.hasCritical ? '<span class="badge badge-danger">Critical</span>' : '<span class="badge badge-success">OK</span>';
                html += '<div class="metric-card" style="cursor:pointer;" onclick="window.openPlantInventoryModal(\''+p.code+'\')">' +
                        '  <div class="d-flex-between align-items-center mb-2">' +
                        '    <strong style="font-size:1.1rem;">'+esc(p.name)+' ('+esc(p.code)+')</strong>' +
                        '    ' + statusBadge +
                        '  </div>' +
                        '  <div class="text-secondary mb-1">Capacity: '+esc(p.capacity)+'</div>' +
                        '  <div class="text-muted" style="font-size:0.85rem;">Products: '+p.items.length+'</div>' +
                        '</div>';
            });
            
            container.innerHTML = html;
        });
    }

    window.openPlantInventoryModal = function(unitCode) {
        var plant = window.plantDataMap[unitCode];
        if(!plant) return;
        
        document.getElementById('plant-modal-title').textContent = plant.name + ' Inventory';
        document.getElementById('add-product-btn').setAttribute('onclick', "window.addNewPlantProduct('"+esc(unitCode)+"')");

        var tbody = document.getElementById('plant-modal-tbody');
        var html = '';
        if(plant.items.length === 0) {
            html = '<tr><td colspan="5" class="text-center text-muted">No inventory records.</td></tr>';
        } else {
            plant.items.forEach(function(i){
                var status = i.isLowStock ? '<span class="badge badge-danger">LOW STOCK</span>' : '<span class="badge badge-success">SUFFICIENT</span>';
                html += '<tr>' +
                        '<td><strong>'+esc(i.productCategory)+'</strong></td>' +
                        '<td>'+formatVol(i.quantity)+'</td>' +
                        '<td>'+formatVol(i.threshold)+'</td>' +
                        '<td>'+status+'</td>' +
                        '<td><button class="btn btn-sm btn-outline" style="padding:0.25rem 0.5rem; font-size:0.8rem;" onclick="window.editPlantInventory('+i.id+', \''+i.quantity+'\', \''+esc(unitCode)+'\')">Edit</button></td>' +
                        '</tr>';
            });
        }
        tbody.innerHTML = html;
        document.getElementById('plant-inventory-modal').classList.remove('hide');
        document.getElementById('plant-inventory-modal').style.display = 'flex';
    };

    window.addNewPlantProduct = function(unitCode) {
        document.getElementById('add-product-unitcode').value = unitCode;
        document.getElementById('add-product-form').reset();
        document.getElementById('add-product-modal').classList.remove('hide');
        document.getElementById('add-product-modal').style.display = 'flex';
    };

    window.closeAddProductModal = function() {
        document.getElementById('add-product-modal').classList.add('hide');
        document.getElementById('add-product-modal').style.display = 'none';
    };

    window.submitNewPlantProduct = function(e) {
        e.preventDefault();
        var unitCode = document.getElementById('add-product-unitcode').value;
        var category = document.getElementById('add-product-category').value;
        var quantity = document.getElementById('add-product-quantity').value;

        if(!category || !quantity) return;

        fetch('/api/admin/plant-inventory', {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify({
                unitCode: unitCode,
                productCategory: category,
                quantity: parseFloat(quantity)
            })
        })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(data){
            if(data.success) {
                closeAddProductModal();
                loadPlantInventory();
                setTimeout(function(){
                    if(window.plantDataMap && window.plantDataMap[unitCode]) {
                        window.openPlantInventoryModal(unitCode);
                    }
                }, 500);
            } else {
                window.showToast(data.message || 'Error adding product', 'error');
            }
        })
        .catch(function(err){ console.error(err); window.showToast('Error adding product', 'error'); });
    };

    window.editPlantInventory = function(id, currentQuantity, unitCode) {
        var newQuantity = prompt('Enter new quantity for this product (MT):', currentQuantity);
        if(newQuantity !== null && newQuantity.trim() !== '') {
            fetch('/api/admin/plant-inventory/' + id, {
                method: 'PUT',
                headers: getHeaders(),
                body: JSON.stringify({ quantity: parseFloat(newQuantity) })
            })
            .then(function(res) { if(!res.ok) return handleApiError(res); return res.json(); })
            .then(function(data) {
                if(data.success) {
                    loadPlantInventory();
                    setTimeout(function(){
                        if(window.plantDataMap && window.plantDataMap[unitCode]) {
                            window.openPlantInventoryModal(unitCode);
                        }
                    }, 500);
                } else {
                    window.showToast(data.message || 'Error updating inventory', 'error');
                }
            })
            .catch(function(err){ console.error(err); window.showToast('Error updating inventory', 'error'); });
        }
    };

    window.closePlantInventoryModal = function() {
        document.getElementById('plant-inventory-modal').classList.add('hide');
        document.getElementById('plant-inventory-modal').style.display = 'none';
    };

    // --- Admin Window Functions ---
    window.openInventoryModal = function() {
        document.getElementById('inventory-modal').style.display = 'flex';
    };
    window.closeInventoryModal = function() {
        document.getElementById('inventory-modal').style.display = 'none';
        document.getElementById('inventory-form').reset();
    };
     window.approveOrder = function(id) {
        if(!confirm("Are you sure you want to approve this order?")) return;
        fetch('/api/admin/orders/'+id+'/approve', { method:'POST', headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(){ loadPendingOrders(); loadAllOrders(); loadInventory(); loadAdminDashboard(); })
        .catch(function(err){ window.showToast(err.message, 'error'); });
    };
    window.rejectOrder = function(id) {
        var reason = prompt("Enter rejection reason:");
        if(reason === null) return;
        fetch('/api/admin/orders/'+id+'/reject', { 
            method:'POST', headers: getHeaders(), body: JSON.stringify({reason: reason}) 
        })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(){ loadPendingOrders(); loadAllOrders(); loadAdminDashboard(); })
        .catch(function(err){ window.showToast(err.message, 'error'); });
    };

    window.viewLedger = function(distId) {
        var ledgerLink = document.querySelector('[data-target="sec-ledger"]');
        if(ledgerLink) ledgerLink.click();
        
        var analysisView = document.getElementById('analysis-view');
        if (analysisView) analysisView.classList.remove('hide');
        
        fetch('/api/admin/distributors/'+distId+'/analysis', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(data){
            document.getElementById('analysis-dist-name').textContent = data.distributor.name;
            document.getElementById('analysis-unit-name').textContent = data.distributor.unit ? data.distributor.unit.name : 'SAIL';
            document.getElementById('analysis-total-sales').textContent = formatVol(data.totalSales);
            document.getElementById('analysis-total-target').textContent = formatVol(data.totalTarget);
        });

        fetch('/api/admin/ledger/'+distId, { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(data){
            document.getElementById('analysis-balance').textContent = formatMoney(data.currentBalance);
            var tbody = document.getElementById('analysis-ledger-tbody');
            if(!data.entries || data.entries.length === 0){
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No ledger entries found.</td></tr>';
                return;
            }
            var html = '';
            data.entries.forEach(function(e){
                var typeClass = e.entryType === 'PAYMENT_RECEIVED' ? 'text-success' : 'text-danger';
                html += '<tr>' +
                        '<td>'+formatDate(e.transactionDate)+'</td>' +
                        '<td><strong class="'+typeClass+'">'+esc(e.entryType)+'</strong></td>' +
                        '<td>'+esc(e.remarks)+'</td>' +
                        '<td>'+formatMoney(e.debit)+'</td>' +
                        '<td>'+formatMoney(e.credit)+'</td>' +
                        '<td><strong>'+formatMoney(e.balance)+'</strong></td>' +
                        '</tr>';
            });
            tbody.innerHTML = html;
        });
    };

    // ========================================================
    //  DISTRIBUTOR PAGE
    // ========================================================
    function initDistributor() {
        loadDistAlerts();
        loadDistInventory();
        loadDistOrders();
        loadDistLedger();

        // Place Order Form
        document.getElementById('order-form').addEventListener('submit', function(e) {
            e.preventDefault();
            var payload = {
                productCategory: document.getElementById('order-category').value,
                quantity: document.getElementById('order-quantity').value
            };
            fetch('/api/distributor/' + userId + '/orders', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify(payload)
            }).then(function(res){
                if(!res.ok) return handleApiError(res);
                return res.json();
            }).then(function(){
                showAlert('order-submit-alert', 'Order placed successfully!', 'success');
                document.getElementById('order-form').reset();
                loadDistOrders();
            }).catch(function(err){
                showAlert('order-submit-alert', err.message, 'error');
            });
        });

        // Payment Form
        document.getElementById('payment-form').addEventListener('submit', function(e) {
            e.preventDefault();
            var payload = {
                orderId: document.getElementById('payment-order-id').value,
                paymentMethod: document.getElementById('payment-method').value,
                cardLastFour: document.getElementById('payment-card').value
            };
            fetch('/api/distributor/' + userId + '/payments', {
                method: 'POST',
                headers: getHeaders(),
                body: JSON.stringify(payload)
            }).then(function(res){
                if(!res.ok) return handleApiError(res);
                return res.json();
            }).then(function(){
                closePaymentModal();
                loadDistOrders();
                loadDistLedger();
            }).catch(function(err){
                window.showToast('Payment Error: ' + err.message, 'error');
            });
        });
    }

    // --- Distributor API Calls ---
    function loadDistAlerts() {
        fetch('/api/distributor/' + userId + '/alerts', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(alerts){
            var box = document.getElementById('alerts-box');
            var list = document.getElementById('alerts-list');
            if(!alerts || alerts.length === 0) {
                box.style.display = 'none';
                return;
            }
            box.style.display = 'block';
            var html = '';
            alerts.forEach(function(a){ html += '<li>'+esc(a.message)+'</li>'; });
            list.innerHTML = html;
        });
    }

    function loadDistInventory() {
        fetch('/api/distributor/' + userId + '/inventory', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(items){
            var tbody = document.getElementById('dist-inventory-tbody');
            if(!tbody) return;
            if(!items || items.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">No inventory records.</td></tr>';
                return;
            }
            var html = '';
            items.forEach(function(i){
                var status = i.isLowStock ? '<span class="badge badge-danger">LOW STOCK</span>' : '<span class="badge badge-success">OK</span>';
                var actionBtn = '<button class="btn btn-sm btn-primary" onclick="window.openSaleModal(\''+esc(i.productCategory)+'\')">Record Sale</button>';
                html += '<tr>' +
                        '<td><strong>'+esc(i.productCategory)+'</strong></td>' +
                        '<td>'+formatVol(i.quantity)+'</td>' +
                        '<td>'+status+'</td>' +
                        '<td>'+actionBtn+'</td>' +
                        '</tr>';
            });
            tbody.innerHTML = html;
        });
    }

    function loadDistOrders() {
        fetch('/api/distributor/' + userId + '/orders', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(orders){
            var tbody = document.getElementById('dist-orders-tbody');
            if(!tbody) return;
            if(!orders || orders.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No orders found.</td></tr>';
                return;
            }
            var html = '';
            orders.forEach(function(o){
                var statusBadge = '';
                var actionBtn = '';
                if(o.status === 'PENDING') statusBadge = '<span class="badge badge-warning">Pending</span>';
                else if(o.status === 'APPROVED') {
                    statusBadge = '<span class="badge badge-info">Approved / Unpaid</span>';
                    actionBtn = '<button class="btn btn-sm btn-primary" onclick="window.openPaymentModal('+o.id+', \''+o.productCategory+'\', '+o.totalPrice+')">Pay Now</button>';
                }
                else if(o.status === 'PAID') statusBadge = '<span class="badge badge-success">Paid</span>';
                else if(o.status === 'REJECTED') {
                    statusBadge = '<span class="badge badge-danger" title="'+esc(o.rejectReason)+'">Rejected</span>';
                }

                html += '<tr>' +
                        '<td>'+formatDate(o.placedAt)+'</td>' +
                        '<td><strong>'+esc(o.productCategory)+'</strong></td>' +
                        '<td>'+formatVol(o.quantity)+'</td>' +
                        '<td>'+formatMoney(o.totalPrice)+'</td>' +
                        '<td>'+statusBadge+'</td>' +
                        '<td>'+(actionBtn || '—')+'</td>' +
                        '</tr>';
            });
            tbody.innerHTML = html;
        });
    }

    function loadDistLedger() {
        fetch('/api/distributor/' + userId + '/ledger', { headers: getHeaders() })
        .then(function(res){ if(!res.ok) return handleApiError(res); return res.json(); })
        .then(function(data){
            document.getElementById('ledger-current-balance').textContent = 'Balance: ' + formatMoney(data.currentBalance);
            
            var tbody = document.getElementById('dist-ledger-tbody');
            if(!tbody) return;
            if(!data.entries || data.entries.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No ledger transactions.</td></tr>';
                return;
            }
            var html = '';
            data.entries.forEach(function(e){
                var typeClass = e.entryType === 'PAYMENT_RECEIVED' ? 'text-success' : 'text-danger';
                html += '<tr>' +
                        '<td>'+formatDate(e.transactionDate)+'</td>' +
                        '<td><strong class="'+typeClass+'">'+esc(e.entryType)+'</strong></td>' +
                        '<td>'+esc(e.remarks)+'</td>' +
                        '<td>'+formatMoney(e.debit)+'</td>' +
                        '<td>'+formatMoney(e.credit)+'</td>' +
                        '<td><strong>'+formatMoney(e.balance)+'</strong></td>' +
                        '</tr>';
            });
            tbody.innerHTML = html;
        });
    }

    // --- Distributor Window Functions ---
    window.openPaymentModal = function(orderId, product, amount) {
        document.getElementById('payment-order-id').value = orderId;
        document.getElementById('payment-order-details').textContent = "Paying for Order #" + orderId + " (" + product + ") - " + formatMoney(amount);
        document.getElementById('payment-modal').style.display = 'flex';
    };
    window.closePaymentModal = function() {
        document.getElementById('payment-modal').style.display = 'none';
        document.getElementById('payment-form').reset();
        document.getElementById('card-field').style.display = 'none';
    };

    window.openSaleModal = function(productCategory) {
        document.getElementById('sale-product-category').value = productCategory;
        document.getElementById('sale-product-details').textContent = "Recording sale for: " + productCategory;
        document.getElementById('sale-modal').style.display = 'flex';
    };
    
    window.closeSaleModal = function() {
        document.getElementById('sale-modal').style.display = 'none';
        document.getElementById('sale-form').reset();
    };

    document.getElementById('sale-form').addEventListener('submit', function(e){
        e.preventDefault();
        var payload = {
            productCategory: document.getElementById('sale-product-category').value,
            quantity: document.getElementById('sale-quantity').value,
            price: document.getElementById('sale-price').value
        };
        fetch('/api/distributor/' + userId + '/record-sale', {
            method: 'POST',
            headers: getHeaders(),
            body: JSON.stringify(payload)
        }).then(function(res){
            if(!res.ok) return handleApiError(res);
            return res.json();
        }).then(function(){
            closeSaleModal();
            loadDistInventory();
            loadDistLedger();
        }).catch(function(err){
            window.showToast('Sale Error: ' + err.message, 'error');
        });
    });

})();

