<template>
  <div>
    <Row :gutter="20">
      <i-col
        :xs="12"
        :md="8"
        :lg="4"
        v-for="(infor, i) in inforCardData"
        :key="`infor-${i}`"
        style="height: 120px;padding-bottom: 10px;"
      >
        <infor-card shadow :color="infor.color" :icon="infor.icon" :icon-size="36">
          <h3>{{infor.value}}</h3>
          <p>{{ infor.title }}</p>
        </infor-card>
      </i-col>
    </Row>
  </div>
</template>

<script>
import InforCard from '_c/info-card'
import { getRealtimeData } from '@/api/data'
export default {
  name: 'home',
  components: {
    InforCard
  },
  data() {
    return {
      icons: [
        'md-beaker',
        'md-flask',
        'ios-flask',
        'ios-beaker',
        'ios-flask-outline',
        'ios-beaker-outline'
      ],
      colors: [
        '#2d8cf0',
        '#ff9900',
        '#E46CBB',
        '#2d8cf0',
        '#19be6b',
        '#ff9900'
      ],
      inforCardData: [],
      timer: undefined
    }
  },
  mounted() {
    this.handleRealtimeData()
    this.timer = setInterval(this.handleRealtimeData, 5000)
  },
  methods: {
    handleRealtimeData() {
      getRealtimeData().then(res => {
        const data = res.data
        this.inforCardData.splice(0, this.inforCardData.length)
        let i = 0
        for (let mtData of data) {
          let card = {
            title: mtData.mt.desp,
            icon: this.icons[i],
            value: mtData.value,
            color: this.colors[i]
          }

          i++
          i %= 6

          this.inforCardData.push(card)
        }
      })
    }
  },
  beforeDestroy() {
    clearInterval(this.timer)
  }
}
</script>

<style lang="less">
.count-style {
  font-size: 50px;
}
</style>
