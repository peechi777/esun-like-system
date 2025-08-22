<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  addOrMergeLike, updateLikeBySn, deleteLikeBySn, listLikesByUser,
  createProduct, listProducts
} from './api'

const userId = ref('A1236456789')

const items = ref([])
const summary = reactive({ sumTotalAmount: 0, sumTotalFee: 0 })

const products = ref([])

const addForm = reactive({ productNo: null, account: '1111999666', orderName: 1 })
const editForm = reactive({ sn: null, account: '', orderName: 1 })
const productForm = reactive({ productName: '', price: null, feeRate: null })

const busy = ref(false)
const msg = ref('')

function setMsg(t){ msg.value = t; setTimeout(()=>msg.value='', 2500) }

async function loadProducts(){
  try{
    const data = await listProducts()
    products.value = data || []
    if (!addForm.productNo && products.value.length){
      addForm.productNo = products.value[0].productNo
    }
  }catch(e){ setMsg('讀取商品清單失敗：' + (e?.message||'')) }
}

async function refreshList(){
  busy.value = true
  try{
    const data = await listLikesByUser(userId.value)
    items.value = data.items ?? []
    summary.sumTotalAmount = data.sumTotalAmount ?? 0
    summary.sumTotalFee = data.sumTotalFee ?? 0
  }catch(e){ setMsg('查詢失敗：' + (e?.message||'')) }
  finally{ busy.value = false }
}

async function onCreateProduct(){
  if (!productForm.productName || productForm.price == null || productForm.feeRate == null){
    setMsg('請完整填寫：商品名稱／價格／手續費率'); return
  }
  if (Number(productForm.price) <= 0){ setMsg('價格需 > 0'); return }
  if (Number(productForm.feeRate) < 0){ setMsg('手續費率不可小於 0'); return }

  busy.value = true
  try{
    const payload = {
      productName: String(productForm.productName).trim(),
      price: Number(productForm.price),
      feeRate: Number(productForm.feeRate)
    }
    const res = await createProduct(payload) 
    setMsg('商品建立成功（No=' + res.productNo + '）')
    await loadProducts()
    addForm.productNo = res.productNo   
    productForm.productName = ''
    productForm.price = null
    productForm.feeRate = null
  }catch(e){ setMsg('商品建立失敗：' + (e?.message||'')) }
  finally{ busy.value = false }
}

async function onAdd(){
  if (!addForm.productNo || !addForm.account || !addForm.orderName){
    setMsg('請填完整新增欄位'); return
  }
  if (Number(addForm.orderName) <= 0){ setMsg('購買數量需 > 0'); return }

  busy.value = true
  try{
    await addOrMergeLike({
      userId: userId.value,
      productNo: Number(addForm.productNo),
      account: String(addForm.account),
      orderName: Number(addForm.orderName)
    })
    setMsg('新增/合併成功'); await refreshList()
  }catch(e){ setMsg('新增失敗：' + (e?.message||'')) }
  finally{ busy.value = false }
}

function startEdit(row){
  editForm.sn = row.sn
  editForm.account = row.account
  editForm.orderName = row.orderName
}
function cancelEdit(){ editForm.sn=null; editForm.account=''; editForm.orderName=1 }

async function onUpdate(){
  if (!editForm.sn){ return }
  if (!editForm.account || !editForm.orderName || Number(editForm.orderName) <= 0){
    setMsg('請填帳號且購買數量必須 > 0'); return
  }
  busy.value = true
  try{
    await updateLikeBySn(editForm.sn, { account: String(editForm.account), orderName: Number(editForm.orderName) })
    setMsg('更新成功'); cancelEdit(); await refreshList()
  }catch(e){ setMsg('更新失敗：' + (e?.message||'')) }
  finally{ busy.value = false }
}

async function onDelete(sn){
  if (!confirm(`確定刪除 SN=${sn} 這筆嗎？`)) return
  busy.value = true
  try{ await deleteLikeBySn(sn); setMsg('刪除成功'); await refreshList() }
  catch(e){ setMsg('刪除失敗：' + (e?.message||'')) }
  finally{ busy.value = false }
}

onMounted(async ()=>{
  await loadProducts()
  await refreshList()
})
</script>

<template>
  <main class="container">
    <h1>金融商品喜好紀錄系統（前端示範）</h1>

    <div class="card">
      <div class="row">
        <label>使用者 ID</label>
        <input v-model="userId" placeholder="A1236456789" />
        <button class="primary" :disabled="busy" @click="refreshList">查詢清單</button>
      </div>
    </div>

    <!-- 新增商品 -->
    <section class="card">
      <h2>新增商品（產品名稱／價格／手續費率）</h2>
      <div class="row">
        <label>商品名稱</label>
        <input v-model="productForm.productName" placeholder="例如：使用者新增商品X" />
      </div>
      <div class="row">
        <label>價格 (price)</label>
        <input type="number" min="0" step="0.01" v-model.number="productForm.price" />
      </div>
      <div class="row">
        <label>手續費率 (feeRate)</label>
        <input type="number" min="0" step="0.0001" v-model.number="productForm.feeRate" />
        <span class="tip">輸入 0.1 代表 10%</span>
      </div>
      <button class="primary" :disabled="busy" @click="onCreateProduct">建立商品</button>
    </section>

    <div class="grid">
      <!-- 新增/合併 喜好 -->
      <section class="card">
        <h2>新增/合併 喜好商品</h2>

        <div class="row">
          <label>產品（由商品清單選擇）</label>
          <select v-model.number="addForm.productNo">
            <option v-for="p in products" :key="p.productNo" :value="p.productNo">
              {{ p.productNo }} - {{ p.productName }}（{{ p.price }} / 費率 {{ p.feeRate }}）
            </option>
          </select>
          <span v-if="!products.length" class="tip">目前沒有商品，請先於上方「新增商品」。</span>
        </div>

        <div class="row">
          <label>扣款帳號 (account)</label>
          <input v-model="addForm.account" />
        </div>
        <div class="row">
          <label>購買數量 (orderName)</label>
          <input type="number" min="1" v-model.number="addForm.orderName" />
        </div>
        <button class="primary" :disabled="busy || !products.length" @click="onAdd">送出新增/合併</button>
      </section>

      <!-- 編輯 -->
      <section class="card">
        <h2>編輯（依 SN 更新）</h2>
        <div class="row">
          <label>SN</label>
          <input v-model="editForm.sn" readonly />
        </div>
        <div class="row">
          <label>扣款帳號 (account)</label>
          <input v-model="editForm.account" />
        </div>
        <div class="row">
          <label>購買數量 (orderName)</label>
          <input type="number" min="1" v-model.number="editForm.orderName" />
        </div>
        <div class="row">
          <button class="primary" :disabled="busy || !editForm.sn" @click="onUpdate">儲存更新</button>
          <button class="ghost" :disabled="busy || !editForm.sn" @click="cancelEdit">取消</button>
        </div>
      </section>
    </div>

    <section class="card">
      <h2>清單（明細）</h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>SN</th>
              <th>產品編號</th>
              <th>產品名稱</th>
              <th>價格</th>
              <th>手續費率</th>
              <th>帳號</th>
              <th>購買數量(orderName)</th>
              <th>手續費</th>
              <th>總金額</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in items" :key="row.sn">
              <td>{{ row.sn }}</td>
              <td>{{ row.productNo }}</td>
              <td>{{ row.productName }}</td>
              <td>{{ row.price }}</td>
              <td>{{ row.feeRate }}</td>
              <td>{{ row.account }}</td>
              <td>{{ row.orderName }}</td>
              <td>{{ row.totalFee }}</td>
              <td>{{ row.totalAmount }}</td>
              <td>
                <button @click="startEdit(row)">編輯</button>
                <button class="danger" @click="onDelete(row.sn)">刪除</button>
              </td>
            </tr>
            <tr v-if="!items.length">
              <td colspan="10" class="empty">目前沒有資料</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card">
      <h2>彙總</h2>
      <div class="row">
        <div>總手續費：<strong>{{ summary.sumTotalFee }}</strong></div>
        <div>預計扣款總金額：<strong>{{ summary.sumTotalAmount }}</strong></div>
      </div>
    </section>

    <div v-if="msg" class="msg">{{ msg }}</div>
    <div v-if="busy" class="loading">處理中...</div>
  </main>
</template>

<style>
:root { font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, "Noto Sans TC", Arial; }
.container { max-width: 1000px; margin: 24px auto; padding: 0 16px; }
h1 { font-size: 22px; margin-bottom: 12px; }
h2 { font-size: 18px; margin-bottom: 8px; }
.card { border: 1px solid #ddd; border-radius: 10px; padding: 12px; margin-bottom: 16px; background: #fff; }
.grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
.row label { width: 180px; }
input, select { padding: 6px 8px; border: 1px solid #ccc; border-radius: 8px; }
button { padding: 6px 10px; border: 1px solid #999; background: #f5f5f5; border-radius: 8px; cursor: pointer; }
button:hover { background: #eee; }
button.ghost { background: transparent; }
button.danger { border-color: #cc3333; color: #cc3333; }
.table-wrap { overflow: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { border-bottom: 1px solid #eee; padding: 8px; text-align: left; }
.empty { text-align: center; color: #777; }
.msg { position: fixed; right: 16px; bottom: 16px; background: #333; color: #fff; padding: 8px 12px; border-radius: 8px; }
.loading { position: fixed; left: 50%; bottom: 16px; transform: translateX(-50%); background: #333; color: #fff; padding: 6px 10px; border-radius: 8px; }

@media (max-width: 860px) {
  .grid { grid-template-columns: 1fr; }
  .row label { width: 140px; }
}

.tip { color:#666; font-size: 12px; }

body { background:#f6f8fa; color:#111; }
.card { background:#fff; border-color:#e5e7eb; }
input, select, textarea { background:#fff !important; color:#111 !important; border:1px solid #cbd5e1; border-radius:8px; }
input::placeholder { color:#9ca3af; }
input:focus, select:focus { outline:2px solid #2563eb; outline-offset:2px; }

table { color:#111; }
th { background:#f3f4f6; }

button { background:#f5f5f5; border:1px solid #d1d5db; color:#111; }
button.primary { background:#2563eb; border-color:#2563eb; color:#fff; font-weight:600; }
button.primary:hover { filter: brightness(0.95); }
button.danger { background:#ef4444; border-color:#ef4444; color:#fff; }
button.ghost { background:transparent; color:#111; border-color:#cbd5e1; }
button:disabled { opacity:.6; cursor:not-allowed; }
</style>
