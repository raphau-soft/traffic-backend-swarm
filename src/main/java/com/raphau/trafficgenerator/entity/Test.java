package com.raphau.trafficgenerator.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="test", schema="traffic_generator")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @OneToOne(mappedBy = "test")
    private TestParameters testParameters;
    
    @OneToMany(mappedBy = "test")
    private List<TrafficGeneratorCpuData> cpuDatas;
    
    @OneToMany(mappedBy = "test")
    private List<TrafficGeneratorTimeData> timeDatas;

    @Column(name = "name")
    private String name;

    @Column(name = "finished")
    private boolean finished;

    public Test() {
    }

	public Test(int id, String name, boolean finished) {
		super();
		this.id = id;
		this.name = name;
		this.finished = finished;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public TestParameters getTestParameters() {
		return testParameters;
	}

	public void setTestParameters(TestParameters testParameters) {
		this.testParameters = testParameters;
	}

	public List<TrafficGeneratorCpuData> getCpuDatas() {
		return cpuDatas;
	}

	public void setCpuDatas(List<TrafficGeneratorCpuData> cpuDatas) {
		this.cpuDatas = cpuDatas;
	}

	public List<TrafficGeneratorTimeData> getTimeDatas() {
		return timeDatas;
	}

	public void setTimeDatas(List<TrafficGeneratorTimeData> timeDatas) {
		this.timeDatas = timeDatas;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	@Override
	public String toString() {
		return "Test{" +
				"id=" + id +
				", name='" + name + '\'' +
				", finished=" + finished +
				'}';
	}
}
