/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.presenter;


import android.content.Context;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openlmis.core.R;
import org.openlmis.core.manager.UserInfoMgr;
import org.openlmis.core.model.User;
import org.openlmis.core.model.repository.ProductRepository;
import org.openlmis.core.model.repository.UserRepository;
import org.openlmis.core.utils.ToastUtil;
import org.openlmis.core.view.View;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class LoginPresenter implements Presenter {

    LoginView view;
    User user;

    @Inject
    UserRepository userRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    Context context;

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void attachView(View v) {
        this.view = (LoginView) v;
    }

    public void startLogin(String userName, String password) {

        if (StringUtils.EMPTY.equals(userName.trim())) {
            view.showEmptyAlert(0);
            return;
        }

        if (StringUtils.EMPTY.equals(password)) {
            view.showEmptyAlert(1);
            return;
        }


        view.startLoading();

        user = new User(userName.trim(), password);
        if (view.isConnectionAvailable()) {
            userRepository.authorizeUser(user, new Callback<UserRepository.UserResponse>() {
                @Override
                public void success(UserRepository.UserResponse userResponse, Response response) {
                    User userInfo = userResponse.getUserInformation();
                    userInfo.setUsername(user.getUsername());
                    userInfo.setPassword(user.getPassword());
                    if (userResponse.getUserInformation() != null) {
                        onLoginSuccess(userInfo);
                    } else {
                        onLoginFailed();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    view.stopLoading();
                    onLoginFailed();
                }
            });
        } else {
            User localUser = userRepository.getUserForLocalDatabase(userName.trim(), password);

            if (localUser == null) {
                view.showInvalidAlert();
            } else {
                UserInfoMgr.getInstance().setUser(user);
                goToNextPage();
            }
            view.stopLoading();
        }
    }


    public void saveUserToLocalDatabase(User user) {
        userRepository.save(user);
    }

    public void onLoginSuccess(User user) {
        saveUserToLocalDatabase(user);
        UserInfoMgr.getInstance().setUser(user);
        getProducts();
    }

    public void goToNextPage() {
        if (view.needInitInventory()) {
            view.goToInitInventory();
        } else {
            view.goToHomePage();
        }
    }

    public void onLoginFailed() {
        view.stopLoading();
        view.showInvalidAlert();
        view.clearPassword();
    }


    public void getProducts() {
        if (!view.hasGetProducts()) {
            productRepository.getProducts(UserInfoMgr.getInstance().getUser().getFacilityCode(), new Callback<ProductRepository.ProductsResponse>() {
                @Override
                public void success(ProductRepository.ProductsResponse programsWithProducts, Response response) {
                    view.stopLoading();
                    saveProductsToLocalDatabase(programsWithProducts);
                    view.setHasGetProducts(true);
                    goToNextPage();
                }

                @Override
                public void failure(RetrofitError error) {
                    view.stopLoading();
                    view.setHasGetProducts(false);
                    ToastUtil.show(R.string.init_product_error);
                }
            });
        } else {
            view.stopLoading();
            goToNextPage();
        }
    }

    private void saveProductsToLocalDatabase(ProductRepository.ProductsResponse response) {
        List<ProductRepository.ProgramsWithProducts> programsWithProducts = response.getProgramsWithProducts();
        for (ProductRepository.ProgramsWithProducts programsWithProduct : programsWithProducts) {
            productRepository.save(programsWithProduct.getProducts());
        }
    }

    public interface LoginView extends View {
        void stopLoading();

        void startLoading();

        void clearPassword();

        void goToHomePage();

        void goToInitInventory();

        boolean needInitInventory();

        void showInvalidAlert();

        void showEmptyAlert(int position);

        boolean isConnectionAvailable();

        boolean hasGetProducts();

        void setHasGetProducts(boolean hasGetProducts);
    }
}
