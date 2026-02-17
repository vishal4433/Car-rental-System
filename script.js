const grid = document.getElementById("carGrid");
const select = document.getElementById("carSelect");
const msg = document.getElementById("message");
const countPill = document.getElementById("countPill");
const searchInput = document.getElementById("searchInput");

document.getElementById("addBtn").addEventListener("click", addCar);
document.getElementById("rentBtn").addEventListener("click", rentCar);
searchInput.addEventListener("input", refreshUI);

window.onload = refreshUI;

async function refreshUI() {
  msg.innerText = "";
  grid.innerHTML = "";
  select.innerHTML = "";

  let cars = [];
  try {
    const res = await fetch("/api/cars");
    cars = await res.json();
  } catch (e) {
    showMessage("❌ Backend not reachable. Start server.");
    return;
  }

  const q = (searchInput.value || "").trim().toLowerCase();
  const filtered = q
    ? cars.filter(c => `${c.brand} ${c.model}`.toLowerCase().includes(q))
    : cars;

  countPill.innerText = `${cars.length} car${cars.length === 1 ? "" : "s"}`;

  // dropdown: available cars
  const availableCars = cars.filter(c => c.available);
  if (availableCars.length === 0) {
    select.innerHTML = `<option value="">No cars available</option>`;
  } else {
    availableCars.forEach(car => {
      select.innerHTML += `<option value="${car.carId}">
        ${escapeHtml(car.brand)} ${escapeHtml(car.model)} (${car.carId})
      </option>`;
    });
  }

  if (filtered.length === 0) {
    grid.innerHTML = `<p class="message">No cars found. Add your first car above 👆</p>`;
    return;
  }

  filtered.forEach((car) => {
    const badge = car.available
      ? `<span class="badge">Available</span>`
      : `<span class="badge red">Rented by ${escapeHtml(car.rentedBy || "User")}</span>`;

    const actions = car.available
      ? `
        <div class="actions">
          <button class="btn danger" onclick="deleteCar('${car.carId}')">Delete</button>
        </div>
      `
      : `
        <div class="actions">
          <button class="btn ghost" onclick="returnCar('${car.carId}')">Return</button>
        </div>
      `;

    grid.innerHTML += `
      <div class="car-card">
        <img src="${car.imageUrl}"
             alt="${escapeHtml(car.brand)} ${escapeHtml(car.model)}"
             onerror="this.src='https://via.placeholder.com/800x450?text=Car+Image'">
        <div class="car-body">
          <div class="car-title">
            <h3>${escapeHtml(car.brand)} ${escapeHtml(car.model)}</h3>
            <div class="price">₹${car.pricePerDay}/day</div>
          </div>
          <div class="meta">${badge}</div>
          ${actions}
        </div>
      </div>
    `;
  });
}

async function addCar() {
  const brand = document.getElementById("brand").value.trim();
  const model = document.getElementById("model").value.trim();
  const pricePerDay = Number(document.getElementById("price").value);
  const imageUrl = document.getElementById("image").value.trim();

  if (!brand || !model || !imageUrl || pricePerDay <= 0) {
    showMessage("❌ Enter all valid car details");
    return;
  }

  const res = await fetch("/api/cars", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ brand, model, pricePerDay: String(pricePerDay), imageUrl }),
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    showMessage("❌ " + (data.message || "Add car failed"));
    return;
  }

  showMessage("✅ Car added & saved");
  clearAddForm();
  refreshUI();
}

async function deleteCar(carId) {
  const ok = confirm(`Delete ${carId}? This cannot be undone.`);
  if (!ok) return;

  const res = await fetch(`/api/cars/${carId}`, { method: "DELETE" });
  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    showMessage("❌ " + (data.message || "Delete failed"));
    return;
  }

  showMessage("✅ Deleted successfully");
  refreshUI();
}

async function rentCar() {
  const customer = document.getElementById("customer").value.trim();
  const carId = document.getElementById("carSelect").value;
  const days = Number(document.getElementById("days").value);

  if (!customer || !carId || days <= 0) {
    showMessage("❌ Enter valid rental details");
    return;
  }

  const res = await fetch("/api/rent", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ customer, carId, days: String(days) }),
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    showMessage("❌ " + (data.message || "Rent failed"));
    return;
  }

  showMessage(`✅ Rented! Total = ₹${data.total}`);
  refreshUI();
}

async function returnCar(carId) {
  const res = await fetch(`/api/return/${carId}`, { method: "POST" });
  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    showMessage("❌ " + (data.message || "Return failed"));
    return;
  }

  showMessage("✅ Car returned");
  refreshUI();
}

function clearAddForm() {
  document.getElementById("brand").value = "";
  document.getElementById("model").value = "";
  document.getElementById("price").value = "";
  document.getElementById("image").value = "";
}

function showMessage(text) {
  msg.innerText = text;
}

function escapeHtml(str) {
  return String(str).replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;"
  }[c]));
}
