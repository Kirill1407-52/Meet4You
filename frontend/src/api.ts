import axios from "axios";
import qs from "qs";
import { User } from "./App";

export const api = axios.create({
    baseURL: "http://localhost:8080/api",
    paramsSerializer: params => qs.stringify(params, { arrayFormat: "repeat" }),
});

export const searchByInterest = (interestType: string) =>
    api.get<User[]>("/users/by-interest", {
        params: { interestType },
    });

export const searchByAllInterests = (interestTypes: string[]) =>
    api.get<User[]>("/users/by-all-interests", {
        params: { interestTypes },
    });

export const searchByAnyInterest = (interestTypes: string[]) =>
    api.get<User[]>("/users/by-any-interest", {
        params: { interestTypes },
    });

export const updateUserInterest = (userId: number, interestId: number, newInterestName: string) => {
    return axios.put(`/api/users/${userId}/interests/${interestId}`, null, {
        params: { newInterestName },
    });
};

export const removeUserInterest = (userId: number, interestName: string) => {
    return axios.delete(`/api/users/${userId}/interests`, {
        params: { interestName },
    });
};
