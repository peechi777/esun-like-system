<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import {
  addOrMergeLike, updateLikeBySn, deleteLikeBySn, listLikesByUser,
  createProduct, listProducts, saveUser, deleteProduct
} from './api'

const userId   = ref('')                                  
const items    = ref([])                                   
const summary  = reactive({ sumTotalAmount: 0, sumTotalFee: 0 })
const userInfo = reactive({ userName: '', email: '' })

const products = ref([])
const indexByProductNo = computed(() => {
  const map = new Map()
  ;(products.value || []).forEach((p, idx) => map.set(p.productNo, idx + 1))
  return map
})
const showIdx = (productNo) => indexByProductNo.value.get(productNo) ?? ''

const addForm  = reactive({ productNo: null, account: '', orderName: 1 })
const editForm = reactive({ sn: null, account: '', orderName: 1 })
const isEditing = ref(false)

const busy = ref(false)
const msg  = ref('')
function setMsg(t){ msg.value = t; setTimeout(()=>msg.value='', 2500) }

async function loadProducts(){
  try{
    const data = await listProducts()
    products.value = (data || []).slice().sort((a,b)=>a.productNo - b.productNo)
    if (!addForm.productNo && products.value.length){
      addForm.productNo = products.value[0].productNo
    }
  }catch(e){
    setMsg('讀取商品清單失敗：' + (e?.message || ''))
  }
}

async function refreshList(){
  const uid = userId.value?.trim()
  if (!uid){
    items.value = []
    summary.sumTotalAmount = 0
    summary.sumTotalFee = 0
    return
  }
  busy.value = true
  try{
    const data = await listLikesByUser(uid)
    items.value = data.items ?? []
    summary.sumTotalAmount = data.sumTotalAmount ?? 0
    summary.sumTotalFee    = data.sumTotalFee ?? 0

    if (!userInfo.userName && !userInfo.email && items.value.length){
      userInfo.userName = items.value[0]?.userName ?? ''
      userInfo.email    = items.value[0]?.email    ?? ''
    }
  }catch(e){
    setMsg('查詢失敗：' + (e?.message || ''))
  }finally{
    busy.value = false
  }
}

const productForm = reactive({ productName: '', price: null, feeRate: null })
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
    productForm.productName = ''; productForm.price = null; productForm.feeRate = null
  }catch(e){
    setMsg('商品建立失敗：' + (e?.message || ''))
  }finally{
    busy.value = false
  }
}

async function onDeleteSelectedProduct(){
  if (!addForm.productNo) { setMsg('請先從下拉選擇一個商品'); return }

  const usedByMe = items.value.some(row => row.productNo === addForm.productNo)
  if (usedByMe){
    alert('此商品仍出現在下方明細中，請先刪除明細項目再嘗試刪除商品。')
    return
  }

  const t = products.value.find(p => p.productNo === addForm.productNo)
  const name = t ? `${t.productName}（No=${t.productNo}）` : `No=${addForm.productNo}`
  if (!confirm(`確定要刪除商品「${name}」嗎？`)) return

  busy.value = true
  try{
    await deleteProduct(addForm.productNo) 
    setMsg('商品已刪除')
    await loadProducts()
    if (!products.value.length) addForm.productNo = null
    else if (!products.value.some(p => p.productNo === addForm.productNo)){
      addForm.productNo = products.value[0].productNo
    }
  }catch(e){
    alert((e?.message || '刪除商品失敗') + '\n（可能其他使用者仍在使用此商品）')
    setMsg('刪除商品失敗：' + (e?.message || ''))
  }finally{
    busy.value = false
  }
}

function startEdit(row){
  editForm.sn       = row.sn
  editForm.account  = row.account
  editForm.orderName= row.orderName
  addForm.account   = row.account
  addForm.orderName = row.orderName
  isEditing.value   = true
  setMsg(`正在編輯：SN=${row.sn}`)
}

function cancelEdit(){
  editForm.sn = null
  editForm.account = ''
  editForm.orderName = 1
  isEditing.value = false
  setMsg('已取消編輯')
}

async function onDelete(sn){
  if (!confirm(`確定刪除 SN=${sn} 這筆嗎？`)) return
  busy.value = true
  try{
    await deleteLikeBySn(sn)
    setMsg('刪除成功')
    await refreshList()
  }catch(e){
    setMsg('刪除失敗：' + (e?.message || ''))
  }finally{
    busy.value = false
  }
}

async function onSubmitAll(){
  const uid  = (userId.value || '').trim()
  const name = (userInfo.userName || '').trim()
  const mail = (userInfo.email || '').trim()
  const acct = (addForm.account || '').trim()
  const qty  = Number(addForm.orderName)

  if (!uid){ setMsg('請先輸入使用者ID'); return }
  if (!name || !mail){ setMsg('請填寫 使用者名稱 / Email'); return }

  busy.value = true
  try{
    await saveUser({ userId: uid, userName: name, email: mail, account: acct })

    if (editForm.sn){ 
      if (!acct || qty <= 0){ setMsg('請填帳號且購買數量必須 > 0'); return }
      await updateLikeBySn(editForm.sn, { account: acct, orderName: qty })
      setMsg(`已更新：SN=${editForm.sn}`)
      cancelEdit()
    }else{            
      const canAdd = addForm.productNo != null && acct && qty > 0
      if (canAdd){
        await addOrMergeLike({
          userId: uid,
          productNo: Number(addForm.productNo),
          account: acct,
          orderName: qty
        })
        setMsg('已送出（使用者已儲存，明細已新增/合併）')
      }else{
        setMsg('已送出（僅更新使用者資料；如要新增明細請選商品並填帳號/數量）')
      }
    }

    await refreshList()
  }catch(e){
    setMsg('送出失敗：' + (e?.message || ''))
  }finally{
    busy.value = false
  }
}

onMounted(async ()=>{
  await loadProducts()   
})
</script>

<template>
  <main class="container">
    <h1>金融商品喜好紀錄系統</h1>

    <!-- 上方輸入區：使用者 + 商品 + 一鍵送出 -->
    <div class="card">
      <div class="row row-wrap">
        <div class="field">
          <label>使用者 ID</label>
          <input
            v-model="userId"
            placeholder="請輸入使用者ID"
            @keyup.enter="onSubmitAll"
          />
        </div>

        <div class="field">
          <label>商品清單</label>
          <select v-model.number="addForm.productNo">
            <option v-for="(p,i) in products" :key="p.productNo" :value="p.productNo">
              商品編號：{{ i + 1 }}，商品名稱：{{ p.productName }}（價格 {{ p.price }} / 費率 {{ p.feeRate }}）
            </option>
          </select>
        </div>

        <div class="buttons-inline">
          <button class="primary" type="button" :disabled="busy" @click="onSubmitAll">
            {{ editForm.sn ? '儲存更新' : '送出' }}
          </button>
          <button v-if="editForm.sn" class="ghost" type="button" :disabled="busy" @click="cancelEdit">
            取消編輯
          </button>
          <button
            class="danger" type="button"
            :disabled="busy || !addForm.productNo || items.some(r => r.productNo === addForm.productNo)"
            @click="onDeleteSelectedProduct">
            刪除此商品
          </button>
        </div>
      </div>

      <div class="row">
        <label>使用者名稱</label>
        <input v-model="userInfo.userName" placeholder="王小明" />
      </div>
      <div class="row">
        <label>聯絡電子信箱</label>
        <input v-model="userInfo.email" placeholder="test@email.com" />
      </div>
      <div class="row">
        <label>扣款帳號 (account)</label>
        <input v-model="addForm.account" placeholder="請輸入扣款帳號" />
      </div>
      <div class="row">
        <label>購買數量</label>
        <input type="number" min="1" v-model.number="addForm.orderName" placeholder="例如：1" />
      </div>
    </div>

    <!-- 新增商品 -->
    <section class="card">
      <h2>新增商品</h2>
      <div class="row">
        <label>商品名稱</label>
        <input v-model="productForm.productName" placeholder="" />
      </div>
      <div class="row">
        <label>價格</label>
        <input type="number" min="0" step="0.01" v-model.number="productForm.price" />
      </div>
      <div class="row">
        <label>手續費率</label>
        <input type="number" min="0" step="0.0001" v-model.number="productForm.feeRate" placeholder="輸入 0.1 代表 10%" />
      </div>
      <button class="primary" type="button" :disabled="busy" @click="onCreateProduct">建立商品</button>
    </section>

    <!-- 明細 -->
    <section class="card">
      <h2>明細</h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>產品編號</th>
              <th>產品名稱</th>
              <th>價格</th>
              <th>手續費率</th>
              <th>帳號</th>
              <th>使用者 Email</th>
              <th>購買數量</th>
              <th>手續費</th>
              <th>總金額</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in items" :key="row.sn">
              <td>{{ showIdx(row.productNo) }}</td>
              <td>{{ row.productName }}</td>
              <td>{{ row.price }}</td>
              <td>{{ row.feeRate }}</td>
              <td>{{ row.account }}</td>
              <td class="email">{{ row.email }}</td>
              <td>{{ row.orderName }}</td>
              <td>{{ row.totalFee }}</td>
              <td>{{ row.totalAmount }}</td>
              <td>
                <button type="button" @click.stop="startEdit(row)">編輯</button>
                <button type="button" class="danger" @click.stop="onDelete(row.sn)">刪除</button>
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
      <h2>總和</h2>
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

.row { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
.field { display:flex; flex-direction: column; gap:6px; min-width: 260px; }
.row-wrap { align-items: flex-end; }
.buttons-inline { display:flex; gap:8px; margin-left:auto; }

.row label { width: 180px; }
input, select { padding: 6px 8px; border: 1px solid #ccc; border-radius: 8px; min-width: 240px; }
.readonly { padding: 6px 0; min-height: 28px; }
.email { word-break: break-all; }

.table-wrap { overflow: auto; }
table { width: 100%; border-collapse: collapse; }
th, td { border-bottom: 1px solid #eee; padding: 8px; text-align: left; }
th { background:#f3f4f6; }
.empty { text-align: center; color: #777; }

.msg { position: fixed; right: 16px; bottom: 16px; background: #333; color: #fff; padding: 8px 12px; border-radius: 8px; }
.loading { position: fixed; left: 50%; bottom: 16px; transform: translateX(-50%); background: #333; color: #fff; padding: 6px 10px; border-radius: 8px; }

button { padding: 6px 10px; border: 1px solid #d1d5db; background: #f5f5f5; border-radius: 8px; cursor: pointer; color:#111; }
button:hover { background: #eee; }
button.primary { background:#2563eb; border-color:#2563eb; color:#fff; font-weight:600; }
button.primary:hover { filter: brightness(0.95); }
button.danger { background:#ef4444; border-color:#ef4444; color:#fff; }
button.ghost { background:transparent; color:#111; border-color:#cbd5e1; }
button:disabled { opacity:.6; cursor:not-allowed; }

@media (max-width: 860px) {
  .row label { width: auto; }
  .buttons-inline { width: 100%; margin-left: 0; }
}

body { background:#f6f8fa; color:#111; }
input, select, textarea { background:#fff !important; color:#111 !important; border:1px solid #cbd5e1; border-radius:8px; }
input::placeholder { color:#9ca3af; }
input:focus, select:focus { outline:2px solid #2563eb; outline-offset:2px; }
</style>
