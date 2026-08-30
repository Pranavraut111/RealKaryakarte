import com.fasterxml.jackson.databind.ObjectMapper;

public class TestJson {
    static class Req {
        private Long memberId;
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
    }
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Req r = mapper.readValue("{\"memberId\":null}", Req.class);
        System.out.println("memberId: " + r.getMemberId());
        
        Req r2 = mapper.readValue("{}", Req.class);
        System.out.println("memberId2: " + r2.getMemberId());
    }
}
