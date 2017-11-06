package team.kc.utils.redis;

import java.io.Serializable;
import java.util.Date;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

public class SpringDataRedisTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
		RedisTemplate<String, User> redisTemplate = (RedisTemplate<String, User>) context.getBean("jedisTemplate");
		// 其中key采取了StringRedisSerializer
		// 其中value采取JdkSerializationRedisSerializer
		ValueOperations<String, User> valueOper = redisTemplate.opsForValue();
		User u1 = new User("zhangsan", 12);
		User u2 = new User("lisi", 25);
		valueOper.set("u:u1", u1);
		valueOper.set("u:u2", u2);
		System.out.println(valueOper.get("u:u1").getName());
		System.out.println(valueOper.get("u:u2").getName());
		
		context.close();
	}

	/**
	 * 如果使用jdk序列化方式，bean必须实现Serializable，且提供getter/setter方法
	 * 
	 * @author qing
	 * 
	 */
	static class User implements Serializable {
		private static final long serialVersionUID = -3766780183428993793L;
		
		private String name;
		private Date created;
		private int age;

		public User() { }

		public User(String name, int age) {
			this.name = name;
			this.age = age;
			this.created = new Date();
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Date getCreated() {
			return created;
		}

		public void setCreated(Date created) {
			this.created = created;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

	}
}
