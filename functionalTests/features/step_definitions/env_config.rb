module EnvConfig

    STRESS_TEST = false

    def self.getConfig()

        dev_env=true

        if dev_env
            {
                username:"superuser",
                password:"password1",
                mmiaSignature:true,
                stockMovementSignature:true,
                viaSignature:true,
            }
        else
            {
                username:"test_user",
                password:"testuser123",
                mmiaSignature:true,
                stockMovementSignature:true,
                viaSignature:true,
            }
        end
    end
end
