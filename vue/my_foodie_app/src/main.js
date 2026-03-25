import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'vant/lib/index.css'
import './style.css'
import { 
    Button, Form, Field, CellGroup, Tab, Tabs, Icon, NavBar, Divider, Cell, 
    Tabbar, TabbarItem, List, Search, Swipe, SwipeItem, Image as VanImage, 
    Sticky, Tag, Badge, AddressList, RadioGroup, Radio, Popup, Area, Switch,
    Loading, Skeleton, Sidebar, SidebarItem, Empty, Stepper, SubmitBar, ActionSheet,
    PullRefresh, Rate, Uploader, NoticeBar, Picker, DatePicker
} from 'vant'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Register Vant components
app.use(Button)
app.use(Form)
app.use(Field)
app.use(CellGroup)
app.use(Tab)
app.use(Tabs)
app.use(Icon)
app.use(NavBar)
app.use(Divider)
app.use(Cell)
app.use(Tabbar)
app.use(TabbarItem)
app.use(List)
app.use(Search)
app.use(Swipe)
app.use(SwipeItem)
app.use(VanImage)
app.use(Sticky)
app.use(Tag)
app.use(Badge)
app.use(AddressList)
app.use(RadioGroup)
app.use(Radio)
app.use(Popup)
app.use(Area)
app.use(Switch)
app.use(Loading)
app.use(Skeleton)
app.use(Sidebar)
app.use(SidebarItem)
app.use(Empty)
app.use(Stepper)
app.use(SubmitBar)
app.use(ActionSheet)
app.use(PullRefresh)
app.use(Rate)
app.use(Uploader)
app.use(NoticeBar)
app.use(Picker)
app.use(DatePicker)

app.use(Icon)
app.use(NavBar)
app.use(Divider)
app.use(Cell)
app.use(Tabbar)
app.use(TabbarItem)
app.use(List)

app.mount('#app')
