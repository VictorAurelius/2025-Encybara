import { IoMdHome } from "react-icons/io";
import { IoDocuments } from "react-icons/io5";
import { MdBarChart, MdDashboard } from "react-icons/md";
import Lottie from 'react-lottie';
import Widget from "components/widget/Widget";
import { useState, useEffect } from "react";
import { API_BASE_URL } from "service/api.config";
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const [userCount, setUserCount] = useState(0);
  const [courseCount, setCourseCount] = useState(0);
  const [questionCount, setQuestionCount] = useState(0);
  const [lessonCount, setLessonCount] = useState(0);
  const navigator = useNavigate();
  useEffect(() => {
    const fetchUser = async () => {
      const res = await fetch(`${API_BASE_URL}/api/v1/users`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('admin_token')}`
          }
        }
      );
      const data = await res.json();
      setUserCount(data.meta.total);
    };
    fetchUser();
    const fetchCourse = async () => {
      const res = await fetch(`${API_BASE_URL}/api/v1/courses`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('admin_token')}`
          }
        }
      );
      const data = await res.json();
      setCourseCount(data.data.totalElements);
    };
    fetchCourse();
    const fetchQuestion = async () => {
      const res = await fetch(`${API_BASE_URL}/api/v1/questions?point=5`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',

          }
        }
      );
      const data = await res.json();
      setQuestionCount(data.data.totalElements);
    };
    fetchQuestion();
    const fetchLesson = async () => {
      const res = await fetch(`${API_BASE_URL}/api/v1/lessons`,
        {
          method: 'GET',
        }
      );
      const data = await res.json();
      setLessonCount(data.data.totalElements);
    };
    fetchLesson();
  }, []);

  return (
    <div>
      <div className="mt-3 grid grid-cols-1 gap-5 md:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-3 3xl:grid-cols-6">
        <Widget
          icon={<MdBarChart className="h-7 w-7" />}
          title={"Số người học"}
          subtitle={userCount.toString()}
          onClick={() => navigator('/admin/user')}
        />
        <Widget
          icon={<IoDocuments className="h-6 w-6" />}
          title={"Tổng khóa học"}
          subtitle={courseCount.toString()}
          onClick={() => navigator('/admin/courses')}
        />
        <Widget
          icon={<MdBarChart className="h-7 w-7" />}
          title={"Tổng số bài học"}
          subtitle={lessonCount.toString()}
          onClick={() => navigator('/admin/courses')}
        />
        <Widget
          icon={<MdDashboard className="h-6 w-6" />}
          title={"Tổng số câu hỏi:"}
          subtitle={questionCount.toString()}
          onClick={() => navigator('/admin/question')}
        />
        <Widget
          icon={<MdBarChart className="h-7 w-7" />}
          title={"Tổng số ngành"}
          subtitle={"4"}
        />

      </div>

      <div className="mt-0 grid grid-cols-2 gap-5 w-full">
        <div className="flex justify-center items-center">
          <Lottie
            options={{
              animationData: require("../../../assets/animations/homepage.json"),
              autoplay: true,
              loop: true
            }}
            height={300}
            width="100%"
          />
        </div>
        <div className="flex justify-center items-center">
          <Lottie
            options={{
              animationData: require("../../../assets/animations/homepage2.json"),
              autoplay: true,
              loop: true
            }}
            height={300}
            width="100%"
          />
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
