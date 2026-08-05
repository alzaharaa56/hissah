import{api}from'./services.js';
import{asset,page,session,toast,loading,empty,money,date,badge,esc,qs,qsa,formObject,numberOrNull,requireAuth,dashboardPath,idParam}from'./core.js';
import{mountPublic,mountApp,mountAuth}from'./layout.js';
const listOf=d=>Array.isArray(d)?d:(d?.content||d?.items||d?.data||[]);
const value=(o,...keys)=>{for(const k of keys)if(o?.[k]!==undefined&&o?.[k]!==null)return o[k];return null};
const pageHeader=(title,subtitle='',actions='')=>`<div class="page-header"><div><h1>${esc(title)}</h1><p class="muted">${esc(subtitle)}</p></div><div class="row wrap">${actions}</div></div>`;
const err=(el,e)=>{if(el)el.innerHTML=`<div class="card"><h3>Unable to load this section</h3><p class="muted">${esc(e.message)}</p><button class="btn btn-secondary" onclick="location.reload()">Try again</button></div>`;toast(e.message,'error')};
const opportunityCard=o=>`<article class="card hover opportunity-card"><div class="row-between"><span class="eyebrow">${esc(value(o,'categoryName','category','sector')||'Subcontracting')}</span>${badge(value(o,'status')||'OPEN')}</div><div><h3>${esc(value(o,'title')||'Work Package')}</h3><p class="muted">${esc((value(o,'scope','description')||'No scope description supplied.').slice(0,150))}</p></div><div class="opportunity-meta"><span class="badge">📍 ${esc(value(o,'location')||'Oman')}</span><span class="badge">💰 ${money(value(o,'budgetMax','budgetMin')||0)}</span><span class="badge">📅 ${date(value(o,'deadline'))}</span></div><div class="opportunity-footer"><small class="muted">${esc(value(o,'eligibilityType')||'OPEN')}</small><a class="btn btn-primary btn-sm" href="${page('public/opportunity-details.html')}?id=${value(o,'id')||''}">View details</a></div></article>`;
export async function landing(){mountPublic(`<section class="hero"><div class="container hero-grid"><div><span class="eyebrow">Empowering Omani construction partnerships</span><h1>Transparent subcontracting, <em class="text-primary">rooted in opportunity.</em></h1><p>Hissah connects main contractors with qualified Omani SMEs, bringing project packages, bids, awards and milestone progress into one organized platform.</p><div class="hero-actions"><a class="btn btn-primary" href="${page('public/opportunities.html')}">Explore opportunities</a><a class="btn btn-secondary" href="${page('public/register.html')}">Join Hissah</a></div></div><div class="hero-art"><img src="${asset('images/hero.svg')}" alt="Omani construction collaboration illustration"></div></div></section><section class="section section-soft"><div class="container"><div style="text-align:center;max-width:720px;margin:0 auto 35px"><h2>A complete subcontracting workflow</h2><p class="muted">From company verification to opportunity publication, bid evaluation, award and milestone tracking.</p></div><div class="grid grid-4">${[['01','Publish','Main contractors create projects and focused work packages.'],['02','Discover','Verified SMEs filter suitable opportunities.'],['03','Bid','Subcontractors submit structured commercial proposals.'],['04','Deliver','Awards and milestones keep progress visible.']].map(x=>`<div class="card"><div class="feature-icon">${x[0]}</div><h3>${x[1]}</h3><p class="muted">${x[2]}</p></div>`).join('')}</div></div></section><section class="section"><div class="container grid grid-2"><div class="card soft"><span class="eyebrow">For main contractors</span><h2>Source capable local partners</h2><p>Break projects into clear packages, compare submissions fairly and record award decisions.</p><a class="btn btn-primary" href="${page('public/register.html')}?role=MAIN_CONTRACTOR">Register as contractor</a></div><div class="card soft"><span class="eyebrow">For SMEs</span><h2>Find work that matches your capability</h2><p>Search open packages, submit bids and track your awards from one dashboard.</p><a class="btn btn-amber" href="${page('public/register.html')}?role=SUBCONTRACTOR">Register as SME</a></div></div></section>`)}
export async function about(){mountPublic(`<section class="section"><div class="container"><span class="eyebrow">About Hissah</span><div class="grid grid-2" style="align-items:center;margin-top:20px"><div><h1>A digital share of opportunity for Omani SMEs.</h1><p class="muted">Hissah — حِصَّة — means a share or portion. The platform gives qualified local businesses clearer access to subcontracting work while giving main contractors a structured way to source, evaluate and manage partners.</p><div class="grid grid-2"><div class="card"><strong>Transparent</strong><p class="muted">Standardized packages and bid information.</p></div><div class="card"><strong>Local</strong><p class="muted">Built around Omani companies, CRs and Riyada-focused eligibility.</p></div></div></div><img class="hero-art" src="${asset('images/hero.svg')}" alt="Construction collaboration"></div></div></section><section class="section section-soft"><div class="container"><h2>How the platform works</h2><div class="grid grid-3"><div class="card"><h3>1. Verify</h3><p>Companies register, complete profiles and upload documents for administrator review.</p></div><div class="card"><h3>2. Collaborate</h3><p>Contractors publish project packages and SMEs submit commercial and technical bids.</p></div><div class="card"><h3>3. Track</h3><p>Awards, milestones, notifications and reports keep delivery organized.</p></div></div></div></section>`)}
export async function login(){const content=`<div class="auth-page"><section class="auth-art"><a class="brand" href="${page('index.html')}"><img src="${asset('logo/hissah-logo.svg')}" alt="Hissah"></a><div><h1>Welcome back.</h1><p>Continue managing projects, bids and awards with confidence.</p></div><img src="${asset('images/hero.svg')}" alt=""></section><section class="auth-form-wrap"><div class="auth-form"><span class="eyebrow">Secure account access</span><h2>Login to Hissah</h2><p class="muted">Use the email and password connected to your account.</p><form id="login-form" class="stack"><div class="input-group"><label>Email</label><input class="input" type="email" name="email" required></div><div class="input-group"><label>Password</label><input class="input" type="password" name="password" required></div><button class="btn btn-primary btn-block">Login</button></form><p>New to Hissah? <a class="text-primary" href="${page('public/register.html')}"><strong>Create an account</strong></a></p></div></section></div>`;mountAuth(content);qs('#login-form').addEventListener('submit',async e=>{e.preventDefault();const btn=e.submitter;btn.disabled=true;btn.textContent='Signing in…';try{const data=await api.auth.login(formObject(e.target));session.set({token:data.token||data.accessToken,role:data.role||data.user?.role,user:data.user||{},company:data.company||{}});toast('Login successful');location.href=dashboardPath(data.role||data.user?.role)}catch(ex){toast(ex.message,'error');btn.disabled=false;btn.textContent='Login'}})}
export async function register(){const preset=new URLSearchParams(location.search).get('role')||'MAIN_CONTRACTOR';mountAuth(`<div class="auth-page"><section class="auth-art"><a class="brand" href="${page('index.html')}"><img src="${asset('logo/hissah-logo.svg')}" alt="Hissah"></a><div><h1>Build your share.</h1><p>Create a company account, then complete verification to use protected platform workflows.</p></div><img src="${asset('images/hero.svg')}" alt=""></section><section class="auth-form-wrap"><div class="auth-form"><span class="eyebrow">Company registration</span><h2>Create your Hissah account</h2><form id="register-form" class="form-grid"><div class="input-group"><label>Full name</label><input class="input" name="fullName" required></div><div class="input-group"><label>Email</label><input class="input" type="email" name="email" required></div><div class="input-group"><label>Password</label><input class="input" type="password" name="password" minlength="8" required></div><div class="input-group"><label>Phone</label><input class="input" name="phone" required></div><div class="input-group"><label>Account role</label><select class="select" name="role"><option value="MAIN_CONTRACTOR" ${preset==='MAIN_CONTRACTOR'?'selected':''}>Main Contractor</option><option value="SUBCONTRACTOR" ${preset==='SUBCONTRACTOR'?'selected':''}>Subcontractor</option></select></div><div class="input-group"><label>Company type</label><select class="select" name="companyType"><option value="MAIN_CONTRACTOR">Main Contractor</option><option value="SUBCONTRACTOR">Subcontractor</option><option value="BOTH">Both</option></select></div><div class="input-group"><label>Legal name</label><input class="input" name="legalName" required></div><div class="input-group"><label>Trading name</label><input class="input" name="tradingName"></div><div class="input-group"><label>CR number</label><input class="input" name="crNumber" required></div><div class="input-group"><label>Governorate</label><select class="select" name="governorate" required>${['Muscat','Dhofar','Musandam','Al Buraimi','Ad Dakhiliyah','North Al Batinah','South Al Batinah','North Ash Sharqiyah','South Ash Sharqiyah','Al Dhahirah','Al Wusta'].map(v=>`<option>${v}</option>`).join('')}</select></div><div class="input-group span-2"><label>Company description</label><textarea class="textarea" name="description"></textarea></div><button class="btn btn-primary span-2">Create account</button></form><p>Already registered? <a class="text-primary" href="${page('public/login.html')}"><strong>Login</strong></a></p></div></section></div>`);const role=qs('[name=role]'),type=qs('[name=companyType]');role.addEventListener('change',()=>type.value=role.value);type.value=preset;qs('#register-form').addEventListener('submit',async e=>{e.preventDefault();const b=formObject(e.target);b.categoryIds=[];try{const data=await api.auth.register(b);session.set({token:data.token||data.accessToken,role:data.role||data.user?.role,user:data.user||{},company:data.company||{}});toast('Registration completed');location.href=dashboardPath(data.role||data.user?.role)}catch(ex){toast(ex.message,'error')}})}
export async function opportunities(){mountPublic(`<section class="section"><div class="container">${pageHeader('Browse Opportunities','Search open subcontracting packages across Oman.')}<div class="card filters"><input class="input" id="keyword" placeholder="Search title or scope"><select class="select" id="location"><option value="">All locations</option><option>Muscat</option><option>Sohar</option><option>Salalah</option><option>Nizwa</option><option>Duqm</option></select><select class="select" id="eligibility"><option value="">All eligibility</option><option>OPEN</option><option>SME_ONLY</option><option>RIYADA_PREFERRED</option><option>RIYADA_REQUIRED</option></select><button class="btn btn-primary" id="search">Search</button></div><div id="opportunities" class="grid grid-3" style="margin-top:24px"></div></div></section>`);const load=async()=>{const el=qs('#opportunities');loading(el);try{const d=await api.packages.search({keyword:qs('#keyword').value,location:qs('#location').value,eligibilityType:qs('#eligibility').value,status:'OPEN',page:0,size:30,sortBy:'publishedAt',sortDirection:'DESC'});const l=listOf(d);el.innerHTML=l.length?l.map(opportunityCard).join(''):empty('No open opportunities','Try changing the filters or check again later.')}catch(e){err(el,e)}};qs('#search').addEventListener('click',load);load()}
export async function opportunityDetails(){mountPublic(`<section class="section"><div class="container"><div id="details"></div></div></section>`);const el=qs('#details');loading(el);try{const o=await api.packages.get(idParam());el.innerHTML=`${pageHeader(value(o,'title')||'Opportunity',value(o,'referenceNumber')||'Open subcontracting package',`<a class="btn btn-primary" href="${page('subcontractor/bid-form.html')}?workPackageId=${o.id}">Submit a bid</a>`)}<div class="grid grid-3"><div class="card" style="grid-column:span 2"><h2>Scope of work</h2><p>${esc(value(o,'scope')||'—')}</p><h3>Requirements</h3><p>${esc(value(o,'requirements')||'—')}</p></div><aside class="card stack"><div><small class="muted">Status</small><br>${badge(o.status)}</div><div><small class="muted">Location</small><strong style="display:block">${esc(o.location||'—')}</strong></div><div><small class="muted">Budget range</small><strong style="display:block">${money(o.budgetMin)} – ${money(o.budgetMax)}</strong></div><div><small class="muted">Deadline</small><strong style="display:block">${date(o.deadline,true)}</strong></div><div><small class="muted">Eligibility</small><strong style="display:block">${esc(o.eligibilityType||'OPEN')}</strong></div></aside></div>`}catch(e){err(el,e)}}
export async function userProfile(){if(!requireAuth())return;mountApp('User Profile');const root=qs('#page-root');root.innerHTML=pageHeader('User Profile','Update the personal details attached to your account.')+`<div class="card"><form id="user-form" class="form-grid"><div class="input-group"><label>Full name</label><input class="input" name="fullName"></div><div class="input-group"><label>Phone</label><input class="input" name="phone"></div><div class="input-group"><label>Email</label><input class="input" name="email" disabled></div><div class="input-group"><label>Role</label><input class="input" name="role" disabled></div><button class="btn btn-primary">Save profile</button></form></div>`;try{const u=await api.user.me(session.user().id);Object.entries(u).forEach(([k,v])=>{const input=qs(`[name=${k}]`);if(input)input.value=v??''})}catch(e){toast(e.message,'error')}qs('#user-form').addEventListener('submit',async e=>{e.preventDefault();try{const d=await api.user.update(session.user().id,{fullName:e.target.fullName.value,phone:e.target.phone.value});session.set({...session.get(),user:{...session.user(),...d}});toast('Profile updated')}catch(ex){toast(ex.message,'error')}})}
export async function companyProfile(){if(!requireAuth(['MAIN_CONTRACTOR','SUBCONTRACTOR']))return;mountApp('Company Profile');const root=qs('#page-root');root.innerHTML=pageHeader('Company Profile','Maintain company details and verification documents.')+`<div class="grid grid-3"><section class="card" style="grid-column:span 2"><form id="company-form" class="form-grid"><div class="input-group"><label>Legal name</label><input class="input" name="legalName"></div><div class="input-group"><label>Trading name</label><input class="input" name="tradingName"></div><div class="input-group"><label>Company type</label><select class="select" name="companyType"><option>MAIN_CONTRACTOR</option><option>SUBCONTRACTOR</option><option>BOTH</option></select></div><div class="input-group"><label>CR number</label><input class="input" name="crNumber"></div><div class="input-group"><label>Governorate</label><input class="input" name="governorate"></div><div class="input-group span-2"><label>Description</label><textarea class="textarea" name="description"></textarea></div><button class="btn btn-primary">Save company profile</button></form></section><aside class="card"><h3>Verification</h3><div id="verification-status"></div><p class="muted">Sensitive changes may return the profile to pending review.</p></aside></div><section class="card" style="margin-top:22px"><div class="row-between responsive"><div><h2>Company documents</h2><p class="muted">Upload CR, Riyada card or supporting documents.</p></div><form id="document-form" class="row wrap"><select class="select" name="documentType"><option>COMMERCIAL_REGISTRATION</option><option>RIYADA_CARD</option><option>TAX_CARD</option><option>COMPANY_PROFILE</option><option>OTHER</option></select><input class="input" name="documentNumber" placeholder="Document number"><input class="input" type="date" name="expiryDate"><input class="input" type="file" name="file" required><button class="btn btn-primary">Upload</button></form></div><div id="documents"></div></section>`;const cid=session.company().id;const loadDocs=async()=>{const el=qs('#documents');loading(el);try{const l=listOf(await api.documents.list(cid));el.innerHTML=l.length?`<div class="table-wrap"><table><thead><tr><th>Type</th><th>Number</th><th>Status</th><th>Expiry</th><th></th></tr></thead><tbody>${l.map(d=>`<tr><td>${esc(d.documentType)}</td><td>${esc(d.documentNumber||'—')}</td><td>${badge(d.verificationStatus)}</td><td>${date(d.expiryDate)}</td><td><button class="btn btn-danger btn-sm delete-doc" data-id="${d.id}">Delete</button></td></tr>`).join('')}</tbody></table></div>`:empty('No documents uploaded','Upload the Commercial Registration to begin verification.');qsa('.delete-doc').forEach(b=>b.onclick=async()=>{if(confirm('Delete this document?')){await api.documents.remove(b.dataset.id);loadDocs()}})}catch(e){err(el,e)}};try{const c=await api.company.me(session.user().id);session.set({...session.get(),company:c});Object.entries(c).forEach(([k,v])=>{const i=qs(`[name=${k}]`);if(i)i.value=v??''});qs('#verification-status').innerHTML=badge(c.verificationStatus)}catch(e){toast(e.message,'error')}qs('#company-form').onsubmit=async e=>{e.preventDefault();try{const c=await api.company.update(cid,formObject(e.target));session.set({...session.get(),company:c});toast('Company profile updated')}catch(ex){toast(ex.message,'error')}};qs('#document-form').onsubmit=async e=>{e.preventDefault();try{await api.documents.upload(cid,new FormData(e.target));toast('Document uploaded');e.target.reset();loadDocs()}catch(ex){toast(ex.message,'error')}};loadDocs()}
export async function notifications(){if(!requireAuth())return;mountApp('Notifications');const root=qs('#page-root');root.innerHTML=pageHeader('Notifications','Updates about verification, bids, awards and milestones.','<button class="btn btn-secondary" id="read-all">Mark all as read</button>')+`<div class="card" id="notifications"></div>`;const load=async()=>{const el=qs('#notifications');loading(el);try{const l=listOf(await api.notifications.list());el.innerHTML=l.length?l.map(n=>`<div class="notification ${n.read?'':'unread'}"><div class="notification-icon">●</div><div style="flex:1"><div class="row-between"><strong>${esc(n.title||n.type||'Notification')}</strong><small>${date(n.createdAt,true)}</small></div><p>${esc(n.message||'')}</p>${n.read?'':`<button class="btn btn-secondary btn-sm mark-read" data-id="${n.id}">Mark read</button>`}</div></div>`).join(''):empty('You are all caught up','New platform notifications will appear here.');qsa('.mark-read').forEach(b=>b.onclick=async()=>{await api.notifications.read(b.dataset.id);load()})}catch(e){err(el,e)}};qs('#read-all').onclick=async()=>{try{await api.notifications.readAll();toast('All notifications marked as read');load()}catch(e){toast(e.message,'error')}};load()}
export async function myAwards(){if(!requireAuth(['MAIN_CONTRACTOR','SUBCONTRACTOR']))return;mountApp('My Awards');const root=qs('#page-root');root.innerHTML=pageHeader('Awards & Milestones','Track active agreements and delivery progress.')+`<div id="awards" class="grid grid-2"></div>`;const el=qs('#awards');loading(el);try{const l=listOf(await api.awards.mine());el.innerHTML=l.length?l.map(a=>`<article class="card hover"><div class="row-between"><h3>Award #${a.id}</h3>${badge(a.status)}</div><p class="muted">Work package ${esc(a.workPackageTitle||'#'+(a.workPackageId||'—'))}</p><div class="grid grid-2"><div><small>Agreed amount</small><strong style="display:block">${money(a.agreedAmount)}</strong></div><div><small>Delivery</small><strong style="display:block">${esc(a.agreedDeliveryDays||'—')} days</strong></div></div><a class="btn btn-primary btn-block" style="margin-top:18px" href="${page('shared/award-details.html')}?id=${a.id}">Manage award</a></article>`).join(''):empty('No awards yet','Awards appear after a bid is selected.')}catch(e){err(el,e)}}
export async function awardDetails(){if(!requireAuth(['MAIN_CONTRACTOR','SUBCONTRACTOR']))return;mountApp('Award Details');const root=qs('#page-root');root.innerHTML=`<div id="award"></div><section class="card" style="margin-top:22px"><div class="row-between"><div><h2>Milestones</h2><p class="muted">Track work from not started through approval.</p></div>${session.role()==='MAIN_CONTRACTOR'?'<button class="btn btn-primary" id="new-milestone">Add milestone</button>':''}</div><div id="milestones"></div></section><div id="modal"></div>`;const aid=idParam();try{const a=await api.awards.get(aid);qs('#award').innerHTML=pageHeader(`Award #${a.id}`,a.workPackageTitle||'Award agreement')+`<div class="grid grid-4"><div class="card stat"><div><small>Status</small><div>${badge(a.status)}</div></div></div><div class="card stat"><div><small>Agreed amount</small><strong>${money(a.agreedAmount)}</strong></div></div><div class="card stat"><div><small>Delivery days</small><strong>${esc(a.agreedDeliveryDays||0)}</strong></div></div><div class="card stat"><div><small>Awarded</small><strong style="font-size:1rem">${date(a.awardedAt||a.createdAt)}</strong></div></div></div>`}catch(e){err(qs('#award'),e)}const load=async()=>{const el=qs('#milestones');loading(el);try{const l=listOf(await api.milestones.list(aid));el.innerHTML=l.length?`<div class="table-wrap"><table><thead><tr><th>Milestone</th><th>Due</th><th>Progress</th><th>Status</th><th>Action</th></tr></thead><tbody>${l.map(m=>`<tr><td><strong>${esc(m.title)}</strong><div class="help">${esc(m.description||'')}</div></td><td>${date(m.dueDate)}</td><td><div class="progress"><span style="width:${Number(m.completionPercent||0)}%"></span></div><small>${m.completionPercent||0}%</small></td><td>${badge(m.status)}</td><td>${session.role()==='SUBCONTRACTOR'?`<button class="btn btn-secondary btn-sm progress-btn" data-id="${m.id}">Update</button> ${m.completionPercent==100&&m.status!=='SUBMITTED'?`<button class="btn btn-primary btn-sm submit-ms" data-id="${m.id}">Submit</button>`:''}`:m.status==='SUBMITTED'?`<button class="btn btn-primary btn-sm approve-ms" data-id="${m.id}">Approve</button>`:'—'}</td></tr>`).join('')}</tbody></table></div>`:empty('No milestones','The main contractor can add the first delivery milestone.');wireMilestones()}catch(e){err(el,e)}};function modal(html){qs('#modal').innerHTML=`<div class="modal-backdrop"><div class="modal">${html}</div></div>`;qs('.close-modal').onclick=()=>qs('#modal').innerHTML=''}function wireMilestones(){qsa('.submit-ms').forEach(b=>b.onclick=async()=>{try{await api.milestones.submit(b.dataset.id);toast('Milestone submitted');load()}catch(e){toast(e.message,'error')}});qsa('.approve-ms').forEach(b=>b.onclick=async()=>{try{await api.milestones.approve(b.dataset.id);toast('Milestone approved');load()}catch(e){toast(e.message,'error')}});qsa('.progress-btn').forEach(b=>b.onclick=()=>{modal(`<div class="row-between"><h2>Update progress</h2><button class="icon-btn close-modal">×</button></div><form id="progress-form" class="stack"><input type="hidden" name="id" value="${b.dataset.id}"><div class="input-group"><label>Completion percentage</label><input class="input" type="number" min="0" max="100" name="completionPercent" required></div><div class="input-group"><label>Evidence note</label><textarea class="textarea" name="evidenceNote" required></textarea></div><button class="btn btn-primary">Save progress</button></form>`);qs('#progress-form').onsubmit=async e=>{e.preventDefault();try{await api.milestones.progress(e.target.id.value,{completionPercent:Number(e.target.completionPercent.value),status:'IN_PROGRESS',evidenceNote:e.target.evidenceNote.value});qs('#modal').innerHTML='';toast('Progress updated');load()}catch(ex){toast(ex.message,'error')}}})}qs('#new-milestone')?.addEventListener('click',()=>{modal(`<div class="row-between"><h2>Add milestone</h2><button class="icon-btn close-modal">×</button></div><form id="milestone-form" class="stack"><div class="input-group"><label>Title</label><input class="input" name="title" required></div><div class="input-group"><label>Description</label><textarea class="textarea" name="description"></textarea></div><div class="input-group"><label>Due date</label><input class="input" type="date" name="dueDate" required></div><button class="btn btn-primary">Create milestone</button></form>`);qs('#milestone-form').onsubmit=async e=>{e.preventDefault();try{await api.milestones.create(aid,formObject(e.target));qs('#modal').innerHTML='';toast('Milestone created');load()}catch(ex){toast(ex.message,'error')}}});load()}
export async function contractorDashboard(){if(!requireAuth(['MAIN_CONTRACTOR']))return;mountApp('Contractor Dashboard');const root=qs('#page-root');root.innerHTML=pageHeader('Contractor Dashboard','A clear view of projects, packages, bids and awards.',`<a class="btn btn-primary" href="${page('contractor/project-form.html')}">Create project</a>`)+`<div id="stats" class="grid grid-4"></div><div class="grid grid-2" style="margin-top:22px"><section class="card"><div class="row-between"><h2>Recent projects</h2><a href="${page('contractor/projects.html')}" class="text-primary">View all</a></div><div id="recent-projects"></div></section><section class="card"><h2>Recent activity</h2><div id="recent-activity"></div></section></div>`;try{const [d,p,n]=await Promise.all([api.reports.dashboard(),api.projects.mine(session.company().id,0,5),api.notifications.recent().catch(()=>[])]);const stats=[['Projects',d.totalProjects],['Work Packages',d.totalWorkPackages],['Submitted Bids',d.submittedBids],['Active Awards',d.activeAwards]];qs('#stats').innerHTML=stats.map((x,i)=>`<div class="card stat"><div><small class="muted">${x[0]}</small><strong>${x[1]??0}</strong></div><div class="stat-icon">${['▦','▤','✎','✓'][i]}</div></div>`).join('');const l=listOf(p);qs('#recent-projects').innerHTML=l.length?l.map(x=>`<div class="notification"><div class="notification-icon">▦</div><div><a href="${page('contractor/project-details.html')}?id=${x.id}"><strong>${esc(x.title)}</strong></a><div>${badge(x.status||'DRAFT')} <small>${esc(x.referenceNumber||'')}</small></div></div></div>`).join(''):empty('No projects yet','Create your first main-contract project.');const nl=listOf(n);qs('#recent-activity').innerHTML=nl.length?nl.slice(0,5).map(x=>`<div class="notification"><div class="notification-icon">●</div><div><strong>${esc(x.title||x.type)}</strong><p class="help">${esc(x.message||'')}</p></div></div>`).join(''):empty('No recent activity','Workflow notifications will appear here.')}catch(e){toast(e.message,'error')}}
export async function projects(){if(!requireAuth(['MAIN_CONTRACTOR']))return;mountApp('My Projects');const root=qs('#page-root');root.innerHTML=pageHeader('My Projects','Create and manage main-contract projects.',`<a class="btn btn-primary" href="${page('contractor/project-form.html')}">New project</a>`)+`<div id="projects" class="grid grid-3"></div>`;const el=qs('#projects');loading(el);try{const l=listOf(await api.projects.mine(session.company().id,0,50));el.innerHTML=l.length?l.map(p=>`<article class="card hover"><div class="row-between">${badge(p.status||'DRAFT')}<small>${esc(p.referenceNumber||'')}</small></div><h3>${esc(p.title)}</h3><p class="muted">📍 ${esc(p.location||'—')} · ${esc(p.sector||'OTHER')}</p><p>${date(p.startDate)} – ${date(p.endDate)}</p><a class="btn btn-primary btn-block" href="${page('contractor/project-details.html')}?id=${p.id}">Open project</a></article>`).join(''):empty('No projects created','Create a project before adding work packages.')}catch(e){err(el,e)}}
export async function projectForm(){if(!requireAuth(['MAIN_CONTRACTOR']))return;mountApp('Project Form');const root=qs('#page-root'),id=idParam();root.innerHTML=pageHeader(id?'Edit Project':'Create Project','Define the parent project before publishing subcontracting packages.')+`<div class="card"><form id="project-form" class="form-grid"><div class="input-group span-2"><label>Project title</label><input class="input" name="title" required></div><div class="input-group"><label>Sector</label><select class="select" name="sector"><option>CONSTRUCTION</option><option>LOGISTICS</option><option>IT</option><option>MAINTENANCE</option><option>SUPPLY</option><option>OTHER</option></select></div><div class="input-group"><label>Location</label><input class="input" name="location" required></div><div class="input-group"><label>Start date</label><input class="input" type="date" name="startDate" required></div><div class="input-group"><label>End date</label><input class="input" type="date" name="endDate" required></div><div class="input-group span-2"><label>Description</label><textarea class="textarea" name="description"></textarea></div><button class="btn btn-primary">${id?'Update':'Create'} project</button></form></div>`;if(id)try{const d=await api.projects.get(id,session.company().id,session.role());Object.entries(d).forEach(([k,v])=>{const i=qs(`[name=${k}]`);if(i)i.value=v??''})}catch(e){toast(e.message,'error')}qs('#project-form').onsubmit=async e=>{e.preventDefault();try{const body=formObject(e.target),d=id?await api.projects.update(id,session.company().id,session.user().id,body):await api.projects.create(session.company().id,session.user().id,body);toast(id?'Project updated':'Project created');location.href=`${page('contractor/project-details.html')}?id=${d.id}`}catch(ex){toast(ex.message,'error')}}}
export async function projectDetails(){if(!requireAuth(['MAIN_CONTRACTOR']))return;mountApp('Project Details');const root=qs('#page-root'),id=idParam();root.innerHTML=`<div id="project"></div><section class="card" style="margin-top:22px"><div class="row-between responsive"><div><h2>Work Packages</h2><p class="muted">Break this project into packages for subcontractor bidding.</p></div><a class="btn btn-primary" href="${page('contractor/work-package-form.html')}?projectId=${id}">Create package</a></div><div id="packages"></div></section>`;try{const p=await api.projects.get(id,session.company().id,session.role());qs('#project').innerHTML=pageHeader(p.title,p.referenceNumber||'',`<a class="btn btn-secondary" href="${page('contractor/project-form.html')}?id=${p.id}">Edit</a><button class="btn btn-primary project-action" data-action="activate">Activate</button><button class="btn btn-secondary project-action" data-action="complete">Complete</button>`)+`<div class="grid grid-4"><div class="card"><small>Status</small><div>${badge(p.status||'DRAFT')}</div></div><div class="card"><small>Sector</small><strong style="display:block">${esc(p.sector)}</strong></div><div class="card"><small>Location</small><strong style="display:block">${esc(p.location)}</strong></div><div class="card"><small>Timeline</small><strong style="display:block;font-size:.9rem">${date(p.startDate)} – ${date(p.endDate)}</strong></div></div><div class="card" style="margin-top:22px"><h3>Project description</h3><p>${esc(p.description||'No description')}</p></div>`;qsa('.project-action').forEach(b=>b.onclick=async()=>{try{await api.projects.action(id,b.dataset.action,session.company().id,session.user().id);toast(`Project ${b.dataset.action}d`);location.reload()}catch(e){toast(e.message,'error')}})}catch(e){err(qs('#project'),e)}const el=qs('#packages');loading(el);try{const l=listOf(await api.packages.byProject(id));el.innerHTML=l.length?`<div class="table-wrap"><table><thead><tr><th>Package</th><th>Budget</th><th>Deadline</th><th>Status</th><th>Actions</th></tr></thead><tbody>${l.map(w=>`<tr><td><strong>${esc(w.title)}</strong><div class="help">${esc(w.location||'')}</div></td><td>${money(w.budgetMin)} – ${money(w.budgetMax)}</td><td>${date(w.deadline,true)}</td><td>${badge(w.status)}</td><td><a class="btn btn-secondary btn-sm" href="${page('contractor/work-package-form.html')}?id=${w.id}&projectId=${id}">Edit</a> ${w.status==='DRAFT'?`<button class="btn btn-primary btn-sm package-action" data-id="${w.id}" data-action="publish">Publish</button>`:''} <a class="btn btn-secondary btn-sm" href="${page('contractor/compare-bids.html')}?workPackageId=${w.id}">Bids</a></td></tr>`).join('')}</tbody></table></div>`:empty('No work packages','Create the first package for this project.');qsa('.package-action').forEach(b=>b.onclick=async()=>{try{await api.packages.action(b.dataset.id,b.dataset.action);toast('Package published');location.reload()}catch(e){toast(e.message,'error')}})}catch(e){err(el,e)}}
// =============================================================
// AI FEATURE 1 INTEGRATION LOCATION
// This function replaces the original workPackageForm() handler.
// The AI panel is inserted as the FIRST section inside #package-form,
// before the hidden projectId field and the normal package form fields.
// The AI click handler is registered after categories/edit data are loaded
// and immediately before the existing package form submit handler.
// =============================================================
export async function workPackageForm() {
  if (!requireAuth(['MAIN_CONTRACTOR'])) return;

  mountApp('Work Package Form');

  const root = qs('#page-root');
  const id = idParam();
  const projectId = idParam('projectId');

  root.innerHTML =
    pageHeader(
      id ? 'Edit Work Package' : 'Create Work Package',
      'Define scope, budget, deadline and SME eligibility.'
    ) +
    `<div class="card">
      <form id="package-form" class="form-grid">

        <!-- AI FEATURE 1: inserted before the existing work-package fields. -->
        <section class="ai-panel span-2" id="ai-work-package-panel">
          <div class="ai-panel-header">
            <div>
              <span class="ai-kicker">Hissah AI Assistant</span>
              <h2>Generate a work-package draft</h2>
              <p class="muted">
                Describe the required work in simple words. The generated draft
                will fill the form below and remain editable before you save it.
              </p>
            </div>
            <span class="ai-spark" aria-hidden="true">✦</span>
          </div>

          <div class="input-group">
            <label for="ai-brief">Brief work description</label>
            <textarea
              class="textarea"
              id="ai-brief"
              rows="4"
              minlength="15"
              maxlength="3000"
              placeholder="Example: Install electrical wiring and lighting for a new office building in Muscat."
            ></textarea>
            <small class="help">Use at least 15 characters.</small>
          </div>

          <div class="row wrap ai-actions">
            <button
              type="button"
              class="btn btn-primary ai-action-button"
              id="generate-ai-package"
            >
              ✨ Generate draft with AI
            </button>
            <span class="help">Review every generated field before creating the package.</span>
          </div>

          <div
            id="ai-package-status"
            class="ai-status"
            role="status"
            aria-live="polite"
          ></div>

          <div id="ai-package-notes" class="ai-draft-notes"></div>
        </section>
        <!-- END AI FEATURE 1 -->

        <input type="hidden" name="projectId" value="${projectId || ''}">

        <div class="input-group span-2">
          <label>Package title</label>
          <input class="input" name="title" required>
        </div>

        <div class="input-group">
          <label>Category</label>
          <select class="select" name="categoryId" id="category-select" required>
            <option value="">Loading categories…</option>
          </select>
        </div>

        <div class="input-group">
          <label>Location</label>
          <input class="input" name="location" required>
        </div>

        <div class="input-group">
          <label>Minimum budget (OMR)</label>
          <input class="input" type="number" step="0.001" name="budgetMin" required>
        </div>

        <div class="input-group">
          <label>Maximum budget (OMR)</label>
          <input class="input" type="number" step="0.001" name="budgetMax" required>
        </div>

        <div class="input-group">
          <label>Deadline</label>
          <input class="input" type="datetime-local" name="deadline" required>
        </div>

        <div class="input-group">
          <label>Eligibility</label>
          <select class="select" name="eligibilityType">
            <option>OPEN</option>
            <option>SME_ONLY</option>
            <option>RIYADA_PREFERRED</option>
            <option>RIYADA_REQUIRED</option>
          </select>
        </div>

        <div class="input-group span-2">
          <label>Scope</label>
          <textarea class="textarea" name="scope" required></textarea>
        </div>

        <div class="input-group span-2">
          <label>Requirements</label>
          <textarea class="textarea" name="requirements"></textarea>
        </div>

        <button class="btn btn-primary">${id ? 'Update' : 'Create'} package</button>
      </form>
    </div>`;

  const packageForm = qs('#package-form');

  try {
    const categories = listOf(await api.categories.active());

    qs('#category-select').innerHTML =
      '<option value="">Choose category</option>' +
      categories
        .map(category => `<option value="${category.id}">${esc(category.name)}</option>`)
        .join('');

    if (id) {
      const currentPackage = await api.packages.get(id);

      Object.entries(currentPackage).forEach(([key, fieldValue]) => {
        const input = qs(`[name=${key}]`);

        if (input) {
          input.value =
            key === 'deadline' && fieldValue
              ? String(fieldValue).slice(0, 16)
              : fieldValue ?? '';
        }
      });
    }
  } catch (error) {
    toast(error.message, 'error');
  }

  // AI FEATURE 1 EVENT HANDLER
  // Added after category/edit-data loading and before packageForm.onsubmit.
  const aiButton = qs('#generate-ai-package');
  const aiBrief = qs('#ai-brief');
  const aiStatus = qs('#ai-package-status');
  const aiNotes = qs('#ai-package-notes');

  const nullableNumber = fieldName => {
    const rawValue = packageForm.elements[fieldName]?.value?.trim();

    if (!rawValue) return null;

    const parsedValue = Number(rawValue);
    return Number.isFinite(parsedValue) ? parsedValue : null;
  };

  const renderAiList = items => {
    const values = Array.isArray(items) ? items : [];

    return values.length
      ? `<ul>${values.map(item => `<li>${esc(item)}</li>`).join('')}</ul>`
      : '<p class="help">No additional suggestion was generated.</p>';
  };

  aiButton.onclick = async () => {
    const brief = aiBrief.value.trim();

    if (brief.length < 15) {
      aiStatus.className = 'ai-status is-error';
      aiStatus.textContent =
        'Please enter a clearer description containing at least 15 characters.';
      aiBrief.focus();
      return;
    }

    const deadlineValue = packageForm.elements.deadline.value || null;

    const requestBody = {
      brief,
      projectId:
        nullableNumber('projectId') ??
        (projectId ? Number(projectId) : null),
      categoryId: nullableNumber('categoryId'),
      location: packageForm.elements.location.value.trim() || null,
      budgetMin: nullableNumber('budgetMin'),
      budgetMax: nullableNumber('budgetMax'),
      deadline: deadlineValue
    };

    aiButton.disabled = true;
    aiStatus.className = 'ai-status is-loading';
    aiStatus.textContent = 'Hissah AI is preparing an editable draft…';
    aiNotes.innerHTML = '';

    try {
      const draft = await api.ai.generateWorkPackage(requestBody);

      packageForm.elements.title.value = draft.title ?? '';
      packageForm.elements.scope.value = draft.scope ?? '';
      packageForm.elements.requirements.value = draft.requirements ?? '';

      aiStatus.className = 'ai-status is-success';
      aiStatus.textContent = draft.mockResponse
        ? 'Development-mode draft generated. Review and edit it before saving.'
        : 'AI draft generated. Review and edit it before saving.';

      aiNotes.innerHTML = `
        <div class="ai-note-grid">
          <section>
            <h3>Suggested deliverables</h3>
            ${renderAiList(draft.deliverables)}
          </section>
          <section>
            <h3>Evaluation criteria</h3>
            ${renderAiList(draft.evaluationCriteria)}
          </section>
          <section>
            <h3>Safety requirements</h3>
            ${renderAiList(draft.safetyRequirements)}
          </section>
          <section>
            <h3>Risk notes</h3>
            ${renderAiList(draft.riskNotes)}
          </section>
        </div>
        <p class="ai-duration">
          Suggested duration:
          <strong>${esc(draft.suggestedDurationDays ?? '—')} days</strong>
        </p>`;
    } catch (error) {
      aiStatus.className = 'ai-status is-error';
      aiStatus.textContent = error.message;
      toast(error.message, 'error');
    } finally {
      aiButton.disabled = false;
    }
  };

  // EXISTING NORMAL CREATE/UPDATE HANDLER
  packageForm.onsubmit = async event => {
    event.preventDefault();

    const body = formObject(event.target);
    body.categoryId = Number(body.categoryId);
    body.projectId = Number(body.projectId);
    body.budgetMin = Number(body.budgetMin);
    body.budgetMax = Number(body.budgetMax);

    try {
      const savedPackage = id
        ? await api.packages.update(id, body)
        : await api.packages.create(projectId, body);

      toast(id ? 'Package updated' : 'Package created');
      location.href =
        `${page('contractor/project-details.html')}?id=${savedPackage.projectId || projectId}`;
    } catch (error) {
      toast(error.message, 'error');
    }
  };
}
// =============================================================
// AI FEATURE 2 INTEGRATION LOCATION
// This function replaces the original compareBids() handler.
// This frontend has no separate contractor work-package-details page.
// Therefore, the matching panel is inserted on the Compare Bids page,
// immediately after pageHeader(...) and before the existing #bids area.
// The page already carries ?workPackageId=..., so no duplicate page is needed.
// =============================================================
export async function compareBids() {
  if (!requireAuth(['MAIN_CONTRACTOR'])) return;

  mountApp('Compare Bids');

  const root = qs('#page-root');
  const workPackageId = idParam('workPackageId');

  root.innerHTML =
    pageHeader(
      'Compare Bids',
      'Review submitted proposals and make a transparent award decision.'
    ) +
    `
      <!-- AI FEATURE 2: inserted after pageHeader and before #bids. -->
      <section class="ai-panel" id="ai-matching-panel">
        <div class="ai-panel-header">
          <div>
            <span class="ai-kicker">Hissah AI Matching</span>
            <h2>Find suitable subcontractors</h2>
            <p class="muted">
              Review verified SMEs against this work package's category,
              location, delivery history and relevant profile information.
            </p>
          </div>
          <span class="ai-spark" aria-hidden="true">✦</span>
        </div>

        <div class="row wrap ai-actions">
          <button
            type="button"
            class="btn btn-primary ai-action-button"
            id="find-ai-matches"
          >
            ✨ Find suitable SMEs
          </button>
          <span class="help">Recommendations support the decision; they do not create an award.</span>
        </div>

        <div
          id="ai-matching-status"
          class="ai-status"
          role="status"
          aria-live="polite"
        ></div>

        <div id="ai-matches-results" class="ai-match-grid"></div>
      </section>
      <!-- END AI FEATURE 2 -->

      <div id="bids"></div>
      <div id="modal"></div>
    `;

  // AI FEATURE 2 EVENT HANDLER
  // Added immediately after the page HTML is mounted and before bids are loaded.
  const matchingButton = qs('#find-ai-matches');
  const matchingStatus = qs('#ai-matching-status');
  const matchingResults = qs('#ai-matches-results');

  const renderMatchList = items => {
    const values = Array.isArray(items) ? items : [];

    return values.length
      ? `<ul>${values.map(item => `<li>${esc(item)}</li>`).join('')}</ul>`
      : '<p class="help">No item was reported.</p>';
  };

  const renderMatchCard = match => {
    const companyName =
      match.tradingName || match.legalName || `Company #${match.companyId || ''}`;

    const categoryNames = Array.isArray(match.categories)
      ? match.categories.join(', ')
      : 'No category information';

    return `
      <article class="ai-match-card">
        <div class="row-between ai-match-heading">
          <span class="ai-match-score">${esc(match.matchScore ?? 0)}% match</span>
          ${match.mockResponse ? '<span class="badge">DEMO</span>' : ''}
        </div>

        <h3>${esc(companyName)}</h3>
        <p class="muted">${esc(match.governorate || 'Location not provided')}</p>
        <p class="ai-category-line">${esc(categoryNames)}</p>

        <div class="ai-score-breakdown">
          <span><strong>${esc(match.baseScore ?? 0)}</strong> platform score</span>
          <span><strong>${esc(match.aiSemanticScore ?? 0)}</strong> AI score</span>
          <span><strong>${esc(match.completedAwards ?? 0)}</strong> completed awards</span>
          <span><strong>${esc(match.approvedMilestones ?? 0)}</strong> approved milestones</span>
        </div>

        <div class="ai-match-reasons">
          <section>
            <h4>Why it matches</h4>
            ${renderMatchList(match.matchReasons)}
          </section>
          <section>
            <h4>Possible gaps</h4>
            ${renderMatchList(match.possibleGaps)}
          </section>
        </div>
      </article>`;
  };

  matchingButton.onclick = async () => {
    if (!workPackageId) {
      matchingStatus.className = 'ai-status is-error';
      matchingStatus.textContent =
        'The workPackageId is missing from this page URL.';
      return;
    }

    matchingButton.disabled = true;
    matchingStatus.className = 'ai-status is-loading';
    matchingStatus.textContent =
      'Hissah AI is reviewing verified subcontractor profiles…';
    matchingResults.innerHTML = '';

    try {
      const matches = listOf(await api.ai.findSubcontractorMatches(workPackageId, 5));

      if (!matches.length) {
        matchingStatus.className = 'ai-status';
        matchingStatus.textContent =
          'No suitable verified subcontractor matches were found.';
        return;
      }

      matchingStatus.className = 'ai-status is-success';
      matchingStatus.textContent = matches[0]?.mockResponse
        ? 'Development-mode matches generated successfully.'
        : 'AI-assisted subcontractor matches generated successfully.';

      matchingResults.innerHTML = matches.map(renderMatchCard).join('');
    } catch (error) {
      matchingStatus.className = 'ai-status is-error';
      matchingStatus.textContent = error.message;
      toast(error.message, 'error');
    } finally {
      matchingButton.disabled = false;
    }
  };

  // EXISTING BID TABLE AND AWARD WORKFLOW
  const bidsElement = qs('#bids');
  loading(bidsElement);

  try {
    const bids = listOf(await api.bids.forPackage(workPackageId));

    bidsElement.innerHTML = bids.length
      ? `<div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Subcontractor</th>
                <th>Amount</th>
                <th>Delivery</th>
                <th>Proposal</th>
                <th>Status</th>
                <th>Decision</th>
              </tr>
            </thead>
            <tbody>
              ${bids
                .map(
                  bid => `<tr>
                    <td>
                      <strong>${esc(
                        bid.companyName ||
                          bid.subcontractorCompanyName ||
                          'Company #' + (bid.subcontractorCompanyId || '')
                      )}</strong>
                    </td>
                    <td>${money(bid.amount)}</td>
                    <td>${esc(bid.deliveryDays || '—')} days</td>
                    <td>${esc((bid.proposalText || '').slice(0, 90))}</td>
                    <td>${badge(bid.status)}</td>
                    <td>
                      <button
                        class="btn btn-secondary btn-sm bid-action"
                        data-id="${bid.id}"
                        data-action="shortlist"
                      >Shortlist</button>
                      <button
                        class="btn btn-primary btn-sm award-bid"
                        data-id="${bid.id}"
                        data-amount="${bid.amount || ''}"
                        data-days="${bid.deliveryDays || ''}"
                      >Award</button>
                      <button
                        class="btn btn-danger btn-sm bid-action"
                        data-id="${bid.id}"
                        data-action="reject"
                      >Reject</button>
                    </td>
                  </tr>`
                )
                .join('')}
            </tbody>
          </table>
        </div>`
      : empty('No bids received', 'Submitted bids will appear here.');

    qsa('.bid-action').forEach(button => {
      button.onclick = async () => {
        const reason = prompt(`Reason for ${button.dataset.action}:`) || '';

        try {
          await api.bids.action(button.dataset.id, button.dataset.action, { reason });
          toast(`Bid ${button.dataset.action}ed`);
          location.reload();
        } catch (error) {
          toast(error.message, 'error');
        }
      };
    });

    qsa('.award-bid').forEach(button => {
      button.onclick = () => {
        qs('#modal').innerHTML = `
          <div class="modal-backdrop">
            <div class="modal">
              <div class="row-between">
                <h2>Award selected bid</h2>
                <button class="icon-btn" id="close">×</button>
              </div>

              <form id="award-form" class="stack">
                <input type="hidden" name="bidId" value="${button.dataset.id}">

                <div class="input-group">
                  <label>Agreed amount</label>
                  <input
                    class="input"
                    name="agreedAmount"
                    type="number"
                    step="0.001"
                    value="${button.dataset.amount}"
                    required
                  >
                </div>

                <div class="input-group">
                  <label>Agreed delivery days</label>
                  <input
                    class="input"
                    name="agreedDeliveryDays"
                    type="number"
                    value="${button.dataset.days}"
                    required
                  >
                </div>

                <div class="input-group">
                  <label>Notes</label>
                  <textarea class="textarea" name="notes"></textarea>
                </div>

                <button class="btn btn-primary">Confirm award</button>
              </form>
            </div>
          </div>`;

        qs('#close').onclick = () => {
          qs('#modal').innerHTML = '';
        };

        qs('#award-form').onsubmit = async event => {
          event.preventDefault();

          const form = formObject(event.target);

          try {
            const award = await api.awards.create(
              workPackageId,
              form.bidId,
              {
                agreedAmount: Number(form.agreedAmount),
                agreedDeliveryDays: Number(form.agreedDeliveryDays),
                notes: form.notes
              }
            );

            toast('Bid awarded');
            location.href = `${page('shared/award-details.html')}?id=${award.id}`;
          } catch (error) {
            toast(error.message, 'error');
          }
        };
      };
    });
  } catch (error) {
    err(bidsElement, error);
  }
}
export async function reports(){if(!requireAuth(['MAIN_CONTRACTOR']))return;mountApp('Reports & Analytics');const root=qs('#page-root');root.innerHTML=pageHeader('Reports & Analytics','Operational totals and recent platform activity.')+`<div id="report"></div>`;const el=qs('#report');loading(el);try{const d=await api.reports.dashboard();const metrics=[['Projects',d.totalProjects||0],['Packages',d.totalWorkPackages||0],['Open Packages',d.openWorkPackages||0],['Bids',d.totalBids||0],['Submitted Bids',d.submittedBids||0],['Active Awards',d.activeAwards||0],['Pending Milestones',d.pendingMilestones||0],['Unread Notifications',d.unreadNotifications||0]];el.innerHTML=`<div class="grid grid-4">${metrics.map(m=>`<div class="card stat"><div><small>${m[0]}</small><strong>${m[1]}</strong></div></div>`).join('')}</div><div class="grid grid-2" style="margin-top:22px"><div class="card"><h2>Activity overview</h2><div class="chart-bars">${metrics.slice(0,6).map((m,i)=>`<div class="chart-bar" style="height:${Math.max(18,Math.min(100,Number(m[1])*12+20))}%"><span>${m[0].split(' ')[0]}</span></div>`).join('')}</div></div><div class="card"><h2>What to watch</h2><div class="timeline"><div class="timeline-item"><strong>Open packages</strong><p class="muted">Close or award packages after evaluation.</p></div><div class="timeline-item"><strong>Pending milestones</strong><p class="muted">Review submitted milestone evidence.</p></div><div class="timeline-item"><strong>Unread notifications</strong><p class="muted">Check recent workflow changes.</p></div></div></div></div>`}catch(e){err(el,e)}}
export async function smeDashboard(){if(!requireAuth(['SUBCONTRACTOR']))return;mountApp('SME Dashboard');const root=qs('#page-root');root.innerHTML=pageHeader('SME Dashboard','Discover opportunities and monitor your bidding progress.',`<a class="btn btn-primary" href="${page('public/opportunities.html')}">Browse opportunities</a>`)+`<div id="sme-stats" class="grid grid-4"></div><div class="grid grid-2" style="margin-top:22px"><section class="card"><div class="row-between"><h2>Open opportunities</h2><a href="${page('public/opportunities.html')}" class="text-primary">View all</a></div><div id="sme-opportunities"></div></section><section class="card"><h2>My recent bids</h2><div id="sme-bids"></div></section></div>`;try{const [r,o,b]=await Promise.all([api.reports.dashboard(),api.packages.search({status:'OPEN',page:0,size:4}),api.bids.mine(0,5)]);qs('#sme-stats').innerHTML=[['My Bids',r.totalBids],['Submitted',r.submittedBids],['Active Awards',r.activeAwards],['Pending Milestones',r.pendingMilestones]].map((m,i)=>`<div class="card stat"><div><small>${m[0]}</small><strong>${m[1]??0}</strong></div><div class="stat-icon">${['▤','✎','✓','◷'][i]}</div></div>`).join('');const ol=listOf(o);qs('#sme-opportunities').innerHTML=ol.length?ol.map(x=>`<div class="notification"><div class="notification-icon">⌕</div><div><a href="${page('public/opportunity-details.html')}?id=${x.id}"><strong>${esc(x.title)}</strong></a><div class="help">${money(x.budgetMax)} · ${date(x.deadline)}</div></div></div>`).join(''):empty('No open packages','Check again later.');const bl=listOf(b);qs('#sme-bids').innerHTML=bl.length?bl.map(x=>`<div class="notification"><div class="notification-icon">▤</div><div><strong>${esc(x.workPackageTitle||'Bid #'+x.id)}</strong><div>${badge(x.status)} · ${money(x.amount)}</div></div></div>`).join(''):empty('No bids yet','Submit your first bid from an open opportunity.')}catch(e){toast(e.message,'error')}}
export async function bidForm(){if(!requireAuth(['SUBCONTRACTOR']))return;mountApp('Submit Bid');const root=qs('#page-root'),workPackageId=idParam('workPackageId'),bidId=idParam('id');root.innerHTML=pageHeader(bidId?'Edit Draft Bid':'Submit Your Bid','Provide a clear commercial proposal and supporting documents.')+`<div class="grid grid-3"><section class="card" style="grid-column:span 2"><form id="bid-form" class="form-grid"><div class="input-group"><label>Bid amount (OMR)</label><input class="input" type="number" step="0.001" name="amount" required></div><div class="input-group"><label>Delivery days</label><input class="input" type="number" min="1" name="deliveryDays" required></div><div class="input-group span-2"><label>Proposal</label><textarea class="textarea" name="proposalText" required></textarea></div><div class="input-group span-2"><label>Supporting document</label><input class="input" type="file" name="documents"><span class="help">PDF or document up to the backend upload limit.</span></div><div class="input-group"><label>Document type</label><select class="select" name="documentTypes"><option>TECHNICAL_PROPOSAL</option><option>COMPANY_PROFILE</option><option>OTHER</option></select></div><div class="row"><button type="button" class="btn btn-secondary" id="save-draft">Save draft</button><button class="btn btn-primary">Submit bid</button></div></form></section><aside class="card"><h3>Before submitting</h3><ul><li>Confirm company verification.</li><li>Review scope and deadline.</li><li>Use OMR and realistic delivery days.</li><li>A company may have only one active bid per package.</li></ul></aside></div>`;if(bidId)try{const d=await api.bids.get(bidId);['amount','deliveryDays','proposalText'].forEach(k=>qs(`[name=${k}]`).value=d[k]??'')}catch(e){toast(e.message,'error')}const submit=async draft=>{const form=qs('#bid-form'),fd=new FormData(form);if(!form.documents.files.length)fd.delete('documents');try{const d=bidId?await api.bids.update(bidId,fd):await api.bids.create(workPackageId,fd,draft);if(!draft&&d.status==='DRAFT')await api.bids.action(d.id,'submit');toast(draft?'Draft saved':'Bid submitted');location.href=page('subcontractor/my-bids.html')}catch(e){toast(e.message,'error')}};qs('#bid-form').onsubmit=e=>{e.preventDefault();submit(false)};qs('#save-draft').onclick=()=>submit(true)}
export async function myBids(){if(!requireAuth(['SUBCONTRACTOR']))return;mountApp('My Bids');const root=qs('#page-root');root.innerHTML=pageHeader('My Bids','Track drafts, submitted proposals and bid decisions.')+`<div id="my-bids"></div>`;const el=qs('#my-bids');loading(el);try{const l=listOf(await api.bids.mine(0,50));el.innerHTML=l.length?`<div class="table-wrap"><table><thead><tr><th>Opportunity</th><th>Amount</th><th>Delivery</th><th>Status</th><th>Created</th><th>Action</th></tr></thead><tbody>${l.map(b=>`<tr><td><strong>${esc(b.workPackageTitle||'Package #'+(b.workPackageId||''))}</strong></td><td>${money(b.amount)}</td><td>${b.deliveryDays||'—'} days</td><td>${badge(b.status)}</td><td>${date(b.createdAt,true)}</td><td>${b.status==='DRAFT'?`<a class="btn btn-secondary btn-sm" href="${page('subcontractor/bid-form.html')}?id=${b.id}&workPackageId=${b.workPackageId}">Edit</a> <button class="btn btn-primary btn-sm bid-submit" data-id="${b.id}">Submit</button>`:['SUBMITTED','SHORTLISTED'].includes(b.status)?`<button class="btn btn-danger btn-sm bid-withdraw" data-id="${b.id}">Withdraw</button>`:'—'}</td></tr>`).join('')}</tbody></table></div>`:empty('No bids yet','Browse opportunities and submit your first proposal.');qsa('.bid-submit').forEach(b=>b.onclick=async()=>{try{await api.bids.action(b.dataset.id,'submit');toast('Bid submitted');location.reload()}catch(e){toast(e.message,'error')}});qsa('.bid-withdraw').forEach(b=>b.onclick=async()=>{if(confirm('Withdraw this bid?'))try{await api.bids.action(b.dataset.id,'withdraw');toast('Bid withdrawn');location.reload()}catch(e){toast(e.message,'error')}})}catch(e){err(el,e)}}
export async function adminDashboard(){if(!requireAuth(['ADMIN']))return;mountApp('Administrator Dashboard');const root=qs('#page-root');root.innerHTML=pageHeader('Platform Administration','Monitor companies and operational activity.',`<a class="btn btn-primary" href="${page('admin/verification-queue.html')}">Review companies</a>`)+`<div id="admin-stats" class="grid grid-4"></div><div class="grid grid-2" style="margin-top:22px"><section class="card"><h2>Pending verification</h2><div id="admin-pending"></div></section><section class="card"><h2>Platform priorities</h2><div class="timeline"><div class="timeline-item"><strong>Verify company evidence</strong><p class="muted">Commercial Registration is required before approval.</p></div><div class="timeline-item"><strong>Maintain categories</strong><p class="muted">Keep work package classification clear.</p></div><div class="timeline-item"><strong>Monitor platform activity</strong><p class="muted">Use dashboard totals and notifications.</p></div></div></section></div>`;try{const [d,p]=await Promise.all([api.reports.dashboard(),api.company.pending()]);qs('#admin-stats').innerHTML=[['Companies',d.totalCompanies],['Projects',d.totalProjects],['Packages',d.totalWorkPackages],['Awards',d.activeAwards]].map(m=>`<div class="card stat"><div><small>${m[0]}</small><strong>${m[1]??0}</strong></div></div>`).join('');const l=listOf(p);qs('#admin-pending').innerHTML=l.length?l.slice(0,6).map(c=>`<div class="notification"><div class="notification-icon">◉</div><div style="flex:1"><strong>${esc(c.legalName)}</strong><div>${badge(c.verificationStatus)} · ${esc(c.crNumber)}</div></div><a class="btn btn-secondary btn-sm" href="${page('admin/verification-queue.html')}?companyId=${c.id}">Review</a></div>`).join(''):empty('Queue is clear','No company is waiting for review.')}catch(e){toast(e.message,'error')}}
export async function verificationQueue(){if(!requireAuth(['ADMIN']))return;mountApp('Verification Queue');const root=qs('#page-root');root.innerHTML=pageHeader('Company Verification Queue','Review company identity and documents before approval.')+`<div id="queue"></div><div id="modal"></div>`;const el=qs('#queue');loading(el);try{const l=listOf(await api.company.pending());el.innerHTML=l.length?`<div class="table-wrap"><table><thead><tr><th>Company</th><th>CR</th><th>Type</th><th>Governorate</th><th>Status</th><th>Decision</th></tr></thead><tbody>${l.map(c=>`<tr><td><strong>${esc(c.legalName)}</strong><div class="help">${esc(c.tradingName||'')}</div></td><td>${esc(c.crNumber)}</td><td>${esc(c.companyType)}</td><td>${esc(c.governorate)}</td><td>${badge(c.verificationStatus)}</td><td><button class="btn btn-secondary btn-sm review-company" data-id="${c.id}">Review</button></td></tr>`).join('')}</tbody></table></div>`:empty('No pending companies','New registrations will appear here.');qsa('.review-company').forEach(b=>b.onclick=async()=>{const c=await api.company.get(b.dataset.id,0,'ADMIN');let docs=[];try{docs=listOf(await api.documents.list(c.id))}catch{}qs('#modal').innerHTML=`<div class="modal-backdrop"><div class="modal"><div class="row-between"><h2>${esc(c.legalName)}</h2><button class="icon-btn" id="close">×</button></div><p><strong>CR:</strong> ${esc(c.crNumber)}<br><strong>Type:</strong> ${esc(c.companyType)}<br><strong>Governorate:</strong> ${esc(c.governorate)}</p><h3>Documents</h3>${docs.length?docs.map(d=>`<div class="notification"><div><strong>${esc(d.documentType)}</strong><div>${badge(d.verificationStatus)} · ${date(d.expiryDate)}</div></div></div>`).join(''):'<p class="muted">No documents available.</p>'}<form id="decision-form" class="stack"><input type="hidden" name="companyId" value="${c.id}"><div class="input-group"><label>Decision</label><select class="select" name="verificationStatus"><option>VERIFIED</option><option>REJECTED</option></select></div><div class="input-group"><label>Reason / note</label><textarea class="textarea" name="reason"></textarea></div><button class="btn btn-primary">Save decision</button></form></div></div>`;qs('#close').onclick=()=>qs('#modal').innerHTML='';qs('#decision-form').onsubmit=async e=>{e.preventDefault();try{await api.company.verify(e.target.companyId.value,session.user().id,{verificationStatus:e.target.verificationStatus.value,reason:e.target.reason.value||null});toast('Verification decision saved');qs('#modal').innerHTML='';location.reload()}catch(ex){toast(ex.message,'error')}}})}catch(e){err(el,e)}}
export async function categories(){if(!requireAuth(['ADMIN']))return;mountApp('Category Management');const root=qs('#page-root');root.innerHTML=pageHeader('Category Management','Maintain classifications used by work packages.','<button class="btn btn-primary" id="new-category">Add category</button>')+`<div id="categories"></div><div id="modal"></div>`;const load=async()=>{const el=qs('#categories');loading(el);try{const l=listOf(await api.categories.all());el.innerHTML=l.length?`<div class="table-wrap"><table><thead><tr><th>Name</th><th>Description</th><th>Parent</th><th>Active</th><th>Action</th></tr></thead><tbody>${l.map(c=>`<tr><td><strong>${esc(c.name)}</strong></td><td>${esc(c.description||'—')}</td><td>${esc(c.parentCategoryId||'—')}</td><td>${badge(c.active?'ACTIVE':'INACTIVE')}</td><td><button class="btn btn-secondary btn-sm edit-category" data-id="${c.id}">Edit</button> ${c.active?`<button class="btn btn-danger btn-sm deactivate-category" data-id="${c.id}">Deactivate</button>`:''}</td></tr>`).join('')}</tbody></table></div>`:empty('No categories','Add the first work category.');qsa('.edit-category').forEach(b=>b.onclick=()=>open(b.dataset.id));qsa('.deactivate-category').forEach(b=>b.onclick=async()=>{if(confirm('Deactivate category?')){await api.categories.deactivate(b.dataset.id);load()}})}catch(e){err(el,e)}};async function open(id){let c={};if(id)c=await api.categories.get(id);qs('#modal').innerHTML=`<div class="modal-backdrop"><div class="modal"><div class="row-between"><h2>${id?'Edit':'Add'} category</h2><button class="icon-btn" id="close">×</button></div><form id="category-form" class="stack"><div class="input-group"><label>Name</label><input class="input" name="name" value="${esc(c.name||'')}" required></div><div class="input-group"><label>Description</label><textarea class="textarea" name="description">${esc(c.description||'')}</textarea></div><div class="input-group"><label>Parent category ID</label><input class="input" type="number" name="parentCategoryId" value="${c.parentCategoryId||''}"></div><button class="btn btn-primary">Save category</button></form></div></div>`;qs('#close').onclick=()=>qs('#modal').innerHTML='';qs('#category-form').onsubmit=async e=>{e.preventDefault();const b=formObject(e.target);b.parentCategoryId=numberOrNull(b.parentCategoryId);try{id?await api.categories.update(id,b):await api.categories.create({...b,active:true});toast('Category saved');qs('#modal').innerHTML='';load()}catch(ex){toast(ex.message,'error')}}}qs('#new-category').onclick=()=>open();load()}
export async function users(){if(!requireAuth(['ADMIN']))return;mountApp('User Accounts');const root=qs('#page-root');root.innerHTML=pageHeader('User Account Management','This prototype page is prepared for the planned administrator user-management API.')+`<div class="card"><div class="row-between responsive"><div><h2>Backend alignment notice</h2><p class="muted">The current backend exposes current-user profile endpoints but does not expose an administrator list/suspend endpoint. The interface below remains ready for that controller.</p></div>${badge('PLANNED')}</div><div class="table-wrap"><table><thead><tr><th>User</th><th>Role</th><th>Company</th><th>Status</th><th>Action</th></tr></thead><tbody><tr><td>Future API record</td><td>—</td><td>—</td><td>${badge('PLANNED')}</td><td><button class="btn btn-secondary btn-sm" disabled>Manage</button></td></tr></tbody></table></div></div>`}
const handlers={landing,about,login,register,opportunities,'opportunity-details':opportunityDetails,'user-profile':userProfile,'company-profile':companyProfile,notifications,'my-awards':myAwards,'award-details':awardDetails,'contractor-dashboard':contractorDashboard,projects,'project-form':projectForm,'project-details':projectDetails,'work-package-form':workPackageForm,'compare-bids':compareBids,reports,'sme-dashboard':smeDashboard,'bid-form':bidForm,'my-bids':myBids,'admin-dashboard':adminDashboard,'verification-queue':verificationQueue,categories,users};
export async function runPage(){const name=document.body.dataset.page;const fn=handlers[name];if(fn)await fn();else document.body.innerHTML='<main class="container section"><h1>Page not found</h1></main>'}
