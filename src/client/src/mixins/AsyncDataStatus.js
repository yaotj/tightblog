export default {
  data() {
    return {
      asyncDataStatus_ready: false,
    };
  },
  methods: {
    asyncDataStatus_fetched() {
      this.asyncDataStatus_ready = true;
      setTimeout(() => this.$emit("ready"), 500);
      //      this.$emit("ready");
    },
  },
};
